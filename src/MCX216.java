import java.util.List;

import com.sk89q.craftbook.CraftBookWorld;
import com.sk89q.craftbook.SignText;
import com.sk89q.craftbook.Vector;
import com.sk89q.craftbook.ic.BaseIC;
import com.sk89q.craftbook.ic.ChipState;

/**
 * Sapling planter
 * 
 * Hybrid variant of MCX206 and MCX203 chest collector
 * 
 * When there is a sapling or seed item drop in range it will auto plant it above the IC.
 * 
 * @author Drathus
 *
 */
public class MCX216 extends BaseIC {

	@Override
	public String getTitle() {

		return "PLANTER";
	}

	@Override
    public boolean requiresPermission() {
        return true;
    }
	
	@Override
    public String validateEnvironment(CraftBookWorld cbworld, Vector pos, SignText sign) {
		
        if (sign.getLine3().length() == 0) {
        	return "Line 3 should contain itemid[@damage] to target";
        } else {
        	String[] lineParts = sign.getLine3().split(":");
        	int[] info = UtilItem.getItemInfoFromParts(lineParts);
        	
        	if(info == null || !plantableItem(info[0])) {
        		return "No plantable item specified";
        	}
        }
		
        if (sign.getLine4().length() != 0) {
        	int yOffset = 0;
        	try {
        		yOffset = Integer.parseInt(sign.getLine4());
        	} catch (NumberFormatException e) {
        		return "Line four must contain a numeric value for a Y offset.";
        	}
        	if (yOffset < 1) {
        		return "Y Offset must be positive";
        	}
        }
		
		return null;
	}
	
	@Override
	public void think(ChipState chip) {
		World world = CraftBook.getWorld(chip.getCBWorld());
		Vector onBlock = Util.getWallSignBack(world, chip.getPosition(), 1);
		Vector target = null;
		int[] info = null;
		int yOffset = 0;
		
        if (chip.getText().getLine3().length() != 0) {
        	String[] lineParts = chip.getText().getLine3().split(":");
        	info = UtilItem.getItemInfoFromParts(lineParts);
        }
        
    	if(info == null || !plantableItem(info[0])) {
    		return;
    	}

    	try {
    		yOffset = Integer.parseInt(chip.getText().getLine4());
    	} catch (NumberFormatException e) {
    		return;
    	}
    	if (yOffset < 1) {
    		return;
    	}

    	target = onBlock.add(0, yOffset, 0);
    	
		if (world.getBlockIdAt(target.getBlockX(), target.getBlockY(), target.getBlockZ()) == 0 && 
			itemPlantableOnBlock(info[0], world.getBlockIdAt(target.getBlockX(), target.getBlockY() - 1, target.getBlockZ()))) {
			
			saplingPlanter sp = new saplingPlanter(world, target, info[0], info[1]);
			etc.getServer().addToServerQueue(sp);
		}
	}

	private boolean plantableItem(int itemId) {
		boolean isPlantable = false;
		
		if (itemId == 6 || itemId == 295 ||	itemId == 372)
			isPlantable = true;
		
		return isPlantable;
	}
	
	private boolean itemPlantableOnBlock(int itemId, int blockId) {
		boolean isPlantable = false;
		
		if (itemId == 6 && (blockId == 2 || blockId == 3)) {
			// Saplings can go on Dirt or Grass
			isPlantable = true;
		} else if (itemId == 295 && blockId == 60) {
			// Seeds can only go on farmland
			isPlantable = true;
		} else if (itemId == 372 && blockId == 88) {
			// Netherwart on soulsand
			isPlantable = true;
		}
		
		return isPlantable;
	}
	
	private class saplingPlanter implements Runnable {
		private final World world;
		private final Vector target;
		private final int itemId;
		private final int damVal;
		
		public saplingPlanter(World world, Vector target, int itemId, int damVal) {
			
			this.world = world;
			this.target = target;
			this.itemId = itemId;
			this.damVal = damVal;
		}

		@Override
		public void run() {

			try {
				List<ItemEntity> items = this.world.getItemList();
				
				if(items == null)
		        	return;
		        
		        for(ItemEntity itemEnt : items) {
		        	OEntityItem eitem = itemEnt.getEntity();
		        	
		        	if(!UtilEntity.isDead(eitem) && eitem.a.a > 0 && eitem.a.c == this.itemId && (this.damVal == -1 || (this.damVal == -1 || eitem.a.j() == this.damVal))) {
						double diffX = target.getBlockX() - itemEnt.getX();
						double diffY = target.getBlockY() - itemEnt.getY();
						double diffZ = target.getBlockZ() - itemEnt.getZ();
						
						if ((diffX * diffX + diffY * diffY + diffZ * diffZ) < 6) {
							itemEnt.destroy();

							world.setBlockAt(getBlockByItem(this.itemId), target.getBlockX(), target.getBlockY(), target.getBlockZ());
							world.setBlockData(target.getBlockX(), target.getBlockY(), target.getBlockZ(), eitem.a.j());

							break;
						}
		        	}
		        }
			} catch (Exception e) {
			}
		}
		
		private int getBlockByItem(int itemId) {
			
			if (itemId == 295) return 59;
			if (itemId == 6) return 6;
			if (itemId == 372) return 115;
				
			return 0;
		}
	}
}
