package li.cil.oc.integration.mcmp

import li.cil.oc.Constants
import li.cil.oc.Settings
import li.cil.oc.api
import li.cil.oc.client.renderer.block.ModelInitialization
import li.cil.oc.client.renderer.block.PrintModel
import mcmultipart.item.PartPlacementWrapper
import mcmultipart.multipart.MultipartRegistry
import net.minecraft.client.renderer.block.model.ModelResourceLocation
import net.minecraftforge.client.event.ModelBakeEvent
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object MCMultiPart {
  final val CableMultipartRawLocation = Settings.resourceDomain + ":" + Constants.BlockName.Cable
  final val PrintMultipartRawLocation = Settings.resourceDomain + ":" + Constants.BlockName.Print
  final val CableMultipartLocation = new ModelResourceLocation(CableMultipartRawLocation, "multipart")
  final val PrintMultipartLocation = new ModelResourceLocation(PrintMultipartRawLocation, "multipart")

  def init(): Unit = {
    MultipartRegistry.registerPart(classOf[PartCable], PartFactory.PartTypeCable.toString)
    MultipartRegistry.registerPart(classOf[PartPrint], PartFactory.PartTypePrint.toString)
    MultipartRegistry.registerPartFactory(PartFactory, PartFactory.PartTypeCable.toString, PartFactory.PartTypePrint.toString)
    MultipartRegistry.registerPartConverter(PartConverter)
    MultipartRegistry.registerReversePartConverter(PartConverter)

    new PartPlacementWrapper(api.Items.get(Constants.BlockName.Cable).createItemStack(1), PartFactory).register(PartFactory.PartTypeCable.toString)
    new PartPlacementWrapper(api.Items.get(Constants.BlockName.Print).createItemStack(1), PartFactory).register(PartFactory.PartTypePrint.toString)

    MinecraftForge.EVENT_BUS.register(this)
  }

  @SubscribeEvent(priority = EventPriority.LOW)
  def onModelBake(e: ModelBakeEvent): Unit = {
    val registry = e.getModelRegistry

    // Replace default cable model with part model to properly handle connection
    // rendering to multipart cables.
    registry.putObject(ModelInitialization.CableBlockLocation, PartCableModel)
    registry.putObject(CableMultipartLocation, PartCableModel)
    registry.putObject(PrintMultipartLocation, PrintModel)
  }
}
