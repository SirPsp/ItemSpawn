package net.playsocket.itemspawn;

import net.canarymod.Canary;
import net.canarymod.api.entity.living.humanoid.Player;
import net.canarymod.api.inventory.Item;
import net.canarymod.api.inventory.ItemType;
import net.canarymod.chat.Colors;
import net.canarymod.chat.MessageReceiver;
import net.canarymod.commandsys.Command;
import net.canarymod.commandsys.CommandListener;

public class ItemSpawnCommands implements CommandListener {

	@Command(aliases = { "reloaditemspawn" },
			description = "reload disallowed items list",
			permissions = { "itemspawn.admin" },
			toolTip = "/reloaditemspawn",
			min = 1)
	public void reloaditemspawnCMD(MessageReceiver caller, String[] c) {
		if (caller instanceof Player){
			Player p = (Player)caller;
			ItemSpawn.getDisallowedList();
			p.notice("Updated disallowed list");
			return;
		}
	}

	@Command(aliases = { "clearinventory", "ci" },
			description = "clear your inventory",
			permissions = { "itemspawn.clear" },
			toolTip = "/ci",
			min = 1)
	public void clearinventoryCMD(MessageReceiver caller, String[] c) {
		if (caller instanceof Player){
			Player p = (Player)caller;
			if (c.length == 1){
				p.getInventory().clearContents();
				p.notice("Inventory cleared");
				return;
			}
			p.getInventory().clearContents();
			p.notice("Inventory cleared");
			return;
		}
	}

	@Command(aliases = { "itemfind", "if" },
			description = "find the name or id of an item",
			permissions = { "itemspawn.itemfind" },
			toolTip = "/if",
			min = 1)
	public void itemfindCMD(MessageReceiver caller, String[] c) {
		if (caller instanceof Player){
			Player p = (Player)caller;
			if (c.length < 2){
				p.notice("Corrent usage is: " + c[0] + " <ID|name>");
				return;
			}
			int id = -1;
			ItemType it = null;
			try{
				id = Integer.parseInt(c[1]);
				it = ItemType.fromId(id);
			}catch(Throwable t){ 
				it = ItemType.fromString(c[1]);
				if (it != null){
					id = it.getId();
				}
			}
			if (isNullItem(id) || it == null){
				p.notice("Item not found");
				return;
			}
			p.message(Colors.YELLOW + "Item Found  =  ID: " + Colors.WHITE + id + Colors.YELLOW + "  Name: " + 
					Colors.WHITE + it.getDisplayName() + Colors.YELLOW + " (" + Colors.WHITE + it.getMachineName() + Colors.YELLOW + ")");
			return;
		}
	}

	@Command(aliases = { "item", "i", "give"},
			description = "spawn an item",
			permissions = { "itemspawn.spawn" },
			toolTip = "/item <ID|Name> [amount] [damage] [player]",
			min = 1)
	public void spawnitemCMD(MessageReceiver caller, String[] c) {
		if (caller instanceof Player){
			Player p = (Player)caller;
			if (c.length == 1){

				p.notice("Corrent usage is: " + c[0] + " <item> [amount] [damage] [player]");
				return;
			}
			int id = 0;
			try{
				id = Integer.parseInt(c[1]);
			}catch(Throwable t){
				try{
					id = ItemType.fromString(c[1]).getId();
				}catch(Throwable tx){
					p.notice("Invalid Item: " + c[1]);
					return;
				}
			}
			if (!p.hasPermission("itemspawn.admin")){
				if (ItemSpawn.disallowed.contains(id)){
					p.notice("You are not allowed to spawn that item");
					return;
				}
			}
			if (isNullItem(id)){
				p.notice("Invalid Item ID: " + id);
				return;
			}
			Item item = Canary.factory().getItemFactory().newItem(ItemType.fromId(id), 0, 1);
			if (c.length == 2){

				addItem(p, item);
				//					p.getWorld().dropItem(p.getLocation(), item);
				p.notice("There you go " + p.getName());
				return;
			}
			if (c.length == 3){
				int amt = 0;
				try{
					amt = Integer.parseInt(c[2]);
				}catch(Throwable t){
					p.notice("Invalid Amount: " + c[2]);
					return;
				}
				item.setAmount(amt);
				//					p.getWorld().dropItem(p.getLocation(), item);
				addItem(p, item);
				p.notice("There you go " + p.getName());
				return;
			}
			if (c.length == 4){
				int amt = 0;
				int dmg = 0;
				try{
					amt = Integer.parseInt(c[2]);
					dmg = Integer.parseInt(c[3]);
				}catch(Throwable t){
					try{
						Player op = Canary.getServer().matchPlayer(c[3]);
						item.setAmount(amt);
						item.setDamage(dmg);
						//							op.getWorld().dropItem(p.getLocation(), item);
						addItem(op, item);
						p.notice("Giving items to " + op.getName());
						op.notice("Received items from " + p.getName());
						return;
					}catch(Throwable tt){
						p.notice("Corrent usage is: " + c[0] + " <item> [amount] [damage] [player]");
						return;
					}
				}
				item.setAmount(amt);
				item.setDamage(dmg);
				//					p.getWorld().dropItem(p.getLocation(), item);
				addItem(p, item);
				p.notice("There you go " + p.getName());
				return;
			}
			if (c.length >= 5){
				int amt = 0;
				int dmg = 0;
				Player op;
				try{
					amt = Integer.parseInt(c[2]);
					dmg = Integer.parseInt(c[3]);
					op = Canary.getServer().matchPlayer(c[4]);
				}catch(Throwable tt){
					p.notice("Corrent usage is: " + c[0] + " <item> [amount] [damage] [player]");
					return;
				}
				item.setAmount(amt);
				item.setDamage(dmg);
				addItem(op, item);
				//					op.getWorld().dropItem(p.getLocation(), item);
				p.notice("Giving items to " + op.getName());
				op.notice("Received items from " + p.getName());
				return;
			}
		}
	}

	public void addItem(Player p, Item itemToAdd){
		Item[] items = p.getInventory().getContents();
		int len = items.length;
		int amt = itemToAdd.getAmount();
		if (amt == 0){
			amt = 1;
		}
		int dmg = itemToAdd.getDamage();
		for (int i = 0; i < len; i++) {
			if (amt > 0){
				Item item = items[i];
				if (item == null || item.getAmount() <= 0) {
					continue;
				}
				if ((item.getId() == itemToAdd.getId()) && (item.getDamage() == dmg)){
					if (item.getAmount() < 64) {
						int total = item.getAmount() + amt;
						if (total > 64){
							amt = total - 64;
							item.setAmount(64);
						}else{
							item.setAmount(total);
							amt = 0;
						}
					}
				}
			}
		}
		if (amt > 0){
			if (amt > 64){
				int x = (int) Math.ceil((double)amt/64.0);
				for (int i = 0; i < x; i++){
					Item newItem = Canary.factory().getItemFactory().newItem(ItemType.fromId(itemToAdd.getId()), 0, 1);
					newItem.setDamage(dmg);
					if (amt > 64){
						newItem.setAmount(64);
						p.getInventory().addItem(newItem);
						amt -= 64;
					}else{
						newItem.setAmount(amt);
						p.getInventory().addItem(newItem);
					}
				}
			}else{
				itemToAdd.setAmount(amt);
				p.getInventory().addItem(itemToAdd);
			}
		}
	}

	public static boolean isNullItem(int id){
		ItemType itype = ItemType.fromId(id);
		if (itype == null){
			return true;
		}
		return false;
	}

}