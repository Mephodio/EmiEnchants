package pm.meh.emienchants;

import net.minecraftforge.fml.common.Mod;

@Mod(Common.MOD_ID)
public class EmiEnchants {
    
    public EmiEnchants() {
    
        // This method is invoked by the Forge mod loader when it is ready
        // to load your mod. You can access Forge and Common code in this
        // project.
    
        // Use Forge to bootstrap the Common mod.
        Common.LOG.info("Hello Forge world!");
        Common.init();
        
    }
}