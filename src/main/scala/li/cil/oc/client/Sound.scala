package li.cil.oc.client

import java.net.MalformedURLException
import java.net.URL
import java.net.URLConnection
import java.net.URLStreamHandler
import java.util.Timer
import java.util.TimerTask
import java.util.UUID
import com.google.common.base.Charsets
import li.cil.oc.OpenComputers
import li.cil.oc.Settings
import net.minecraft.client.Minecraft
import net.minecraft.client.audio.ITickableSound
import net.minecraft.client.audio.LocatableSound
import net.minecraft.client.audio.SoundEngine
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.ResourceLocation
import net.minecraft.util.SoundCategory
import net.minecraftforge.eventbus.api.{EventPriority, SubscribeEvent}
import net.minecraftforge.event.TickEvent.ClientTickEvent
import net.minecraftforge.event.world.WorldEvent
import scala.collection.mutable
import scala.ref.WeakReference

object Sound {
  private val sources = mutable.WeakHashMap.empty[TileEntity, PseudoLoopingStream]

  private val commandQueue = mutable.PriorityQueue.empty[Command]

  private val updateTimer = new Timer("OpenComputers-SoundUpdater", true)
  if (Settings.get.soundVolume > 0) {
    updateTimer.scheduleAtFixedRate(new TimerTask {
      override def run() {
        sources.synchronized(Sound.updateCallable = Some(() => processQueue()))
      }
    }, 500, 50)
  }

  private var updateCallable = None: Option[() => Unit]

  private def processQueue() {
    if (commandQueue.nonEmpty) {
      commandQueue.synchronized {
        while (commandQueue.nonEmpty && commandQueue.head.when < System.currentTimeMillis()) {
          if (commandQueue.head.tileEntity.get.isEmpty) {
            commandQueue.dequeue()
          } else {
            try commandQueue.dequeue()() catch {
              case t: Throwable => OpenComputers.log.warn("Error processing sound command.", t)
            }
          }
        }
      }
    }
  }

  def startLoop(tileEntity: TileEntity, name: String, volume: Float = 1f, delay: Long = 0) {
    if (Settings.get.soundVolume > 0) {
      commandQueue.synchronized {
        commandQueue += new StartCommand(System.currentTimeMillis() + delay, new WeakReference[TileEntity](tileEntity), name, volume)
      }
    }
  }

  def stopLoop(tileEntity: TileEntity) {
    if (Settings.get.soundVolume > 0) {
      commandQueue.synchronized {
        commandQueue += new StopCommand(new WeakReference[TileEntity](tileEntity))
      }
    }
  }

  def updatePosition(tileEntity: TileEntity) {
    if (Settings.get.soundVolume > 0) {
      commandQueue.synchronized {
        commandQueue += new UpdatePositionCommand(new WeakReference[TileEntity](tileEntity))
      }
    }
  }

  @SubscribeEvent
  def onTick(e: ClientTickEvent) {
    sources.synchronized {
      updateCallable.foreach(_ ())
      updateCallable = None
    }
  }

  @SubscribeEvent(priority = EventPriority.LOWEST)
  def onWorldUnload(event: WorldEvent.Unload) {
    commandQueue.synchronized(commandQueue.clear())
    sources.synchronized(try sources.foreach(_._2.stop()) catch {
      case _: Throwable => // Ignore.
    })
    sources.clear()
  }

  private abstract class Command(val when: Long, val tileEntity: WeakReference[TileEntity]) extends Ordered[Command] {
    def apply(): Unit

    override def compare(that: Command) = (that.when - when).toInt
  }

  private class StartCommand(when: Long, tileEntity: WeakReference[TileEntity], val name: String, val volume: Float) extends Command(when, tileEntity) {
    override def apply() {
      tileEntity.get match {
        case Some(te) =>
          sources.synchronized {
            val current = sources.getOrElse(te, null)
            if (current == null || !current.getLocation.getPath.equals(name)) {
              if (current != null) current.stop()
              // the local variable should guard this weak reference
              sources(te) = new PseudoLoopingStream(tileEntity, volume, name)
            }
          }
        case _ => // race condition, ignore
      }
    }
  }

  private class StopCommand(tileEntity: WeakReference[TileEntity]) extends Command(System.currentTimeMillis() + 1, tileEntity) {
    override def apply() {
      tileEntity.get match {
        case Some(te) =>
          sources.synchronized {
            sources.remove(te) match {
              case Some(sound) => sound.stop()
              case _ =>
            }
          }
          commandQueue.synchronized {
            // Remove all other commands for this tile entity from the queue. This
            // is inefficient, but we generally don't expect the command queue to
            // be very long, so this should be OK.
            commandQueue ++= commandQueue.dequeueAll.filter(_.tileEntity.get.forall(_ == te))
          }
        case _ => // race condition, ignore
      }
    }
  }

  private class UpdatePositionCommand(tileEntity: WeakReference[TileEntity]) extends Command(System.currentTimeMillis(), tileEntity) {
    override def apply() {
      tileEntity.get match {
        case Some(te) =>
          sources.synchronized {
            sources.get(tileEntity.get.get) match {
              case Some(sound) => sound.updatePosition()
              case _ =>
            }
          }
        case _ => // race condition, ignore
      }
    }
  }

  private class PseudoLoopingStream(val tileEntity: WeakReference[TileEntity], val subVolume: Float, name: String)
    extends LocatableSound(new ResourceLocation(OpenComputers.ID, name), SoundCategory.BLOCKS) with ITickableSound {

    var stopped = false
    volume = subVolume * Settings.get.soundVolume
    relative = tileEntity != null
    looping = true
    updatePosition()

    def updatePosition() {
      tileEntity.get match {
        case Some(te) =>
          val pos = te.getBlockPos
          x = pos.getX + 0.5
          y = pos.getY + 0.5
          z = pos.getZ + 0.5
        case _ => stop()
      }
    }

    override def canStartSilent() = true

    override def isStopped() = stopped

    // Required by ITickableSound, which is required to update position while playing
    override def tick() = ()

    def stop() {
      stopped = true
      looping = false
    }
  }
}
