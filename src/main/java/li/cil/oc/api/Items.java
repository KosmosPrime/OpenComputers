package li.cil.oc.api;

import li.cil.oc.api.detail.ItemInfo;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import java.util.concurrent.Callable;

/**
 * Access to item definitions for all blocks and items provided by
 * OpenComputers.
 */
public final class Items {
    /**
     * Get a descriptor object for the block or item with the specified name.
     * <p/>
     * The names are the same as the ones used in the recipe files. An info
     * object can be used to retrieve both the block and item instance of the
     * item, if available. It can also be used to create a new item stack of
     * the item.
     * <p/>
     * Note that these methods should <em>not</em> be called in the pre-init phase,
     * since the {@link li.cil.oc.api.API#items} may not have been initialized
     * at that time. Only start calling these methods in the init phase or later.
     *
     * @param name the name of the item to get the descriptor for.
     * @return the descriptor for the item with the specified name, or
     * <tt>null</tt> if there is no such item.
     */
    public static ItemInfo get(String name) {
        if (API.items != null)
            return API.items.get(name);
        return null;
    }

    /**
     * Get a descriptor object for the block or item represented by the
     * specified item stack.
     *
     * @param stack the stack to get the descriptor for.
     * @return the descriptor for the specified item stack, or <tt>null</tt>
     * if the stack is not a valid OpenComputers item or block.
     */
    public static ItemInfo get(ItemStack stack) {
        if (API.items != null)
            return API.items.get(stack);
        return null;
    }

    /**
     * Register a single loot floppy disk.
     * <p/>
     * The disk will be listed in the creative tab of OpenComputers.
     * <p/>
     * The specified factory callable will be used to generate a new file
     * system when the loot disk is used as a component. The specified name
     * will be used as the label for the loot disk, while the location serves
     * as the identifier to select the corresponding factory method.
     * <p/>
     * To use some directory in your mod JAR as the directory provided by the
     * loot disk, use {@link FileSystem#fromResource} in your callable.
     * <p/>
     * If <tt>doRecipeCycling</tt> is <tt>true</tt>, the floppy disk will be
     * included in the floppy disk recipe cycle if that is enabled.
     * <p/>
     * Call this in the init phase or later, <em>not</em> in pre-init.
     *
     * @param name    the label and identifier to use for the loot disk.
     * @param loc     the location where the disk's contents are stored.
	 * @param color   the color of the disk, as a Minecraft color.
     * @param factory the callable to call for creating file system instances.
     * @param doRecipeCycling whether to include this floppy disk in floppy disk cycling.
     * @return an item stack representing the registered loot disk, to allow
     * adding a recipe for your loot disk, for example.
     */
    public static ItemStack registerFloppy(String name, ResourceLocation loc, DyeColor color,
        Callable<li.cil.oc.api.fs.FileSystem> factory, boolean doRecipeCycling) {

        if (API.items != null)
            return API.items.registerFloppy(name, loc, color, factory, doRecipeCycling);
        return ItemStack.EMPTY;
    }

    /**
     * Register a single custom EEPROM.
     * <p/>
     * The EEPROM will be listed in the creative tab of OpenComputers.
     * <p/>
     * The EEPROM will be initialized with the specified code and data byte
     * arrays. For script code (e.g. a Lua script) use <tt>String.getBytes("UTF-8")</tt>.
     * You can omit any of the arguments by passing <tt>null</tt>.
     *
     * @param name     the label of the EEPROM.
     * @param code     the code section of the EEPROM.
     * @param data     the data section of the EEPROM.
     * @param readonly whether the code section is read-only.
     * @return an item stack representing the registered EEPROM, to allow
     * adding a recipe for your custom BIOS, for example.
     */
    public static ItemStack registerEEPROM(String name, byte[] code, byte[] data, boolean readonly) {
        if (API.items != null)
            return API.items.registerEEPROM(name, code, data, readonly);
        return ItemStack.EMPTY;
    }

    // ----------------------------------------------------------------------- //

    private Items() {
    }
}
