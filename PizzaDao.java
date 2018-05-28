package com.my.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import com.my.dao.PizzaDao.ItemMapper;
import com.my.dao.PizzaDao.cartMapper;
import com.my.dao.PizzaDao.intMapper;
import com.my.model.Item;

@Component
public class PizzaDao {
	@Autowired
	JdbcTemplate jdbcTemplate;

	/* GET MENU */
	public ArrayList<Item> getMenu() {
		System.out.println("Fetching Menu from the SHOP");
		
		String sql = "select * from menu"; 
		List<Item> menu = jdbcTemplate.query(sql, new ItemMapper());
		
		return (ArrayList<Item>) menu;
	}
	public ArrayList<Item> getMenuItem(long itemid) {
		System.out.println("ITEM ID = "+itemid);
		String sql = "select * from menu where itemid = " + itemid; 
		List<Item> menu = jdbcTemplate.query(sql, new ItemMapper());
		System.out.println("Menu size = " +menu.size());
		for(Item item:menu)
		{
			System.out.println(item);
		}
		return (ArrayList<Item>) menu;
	}
	
	public long getNewMenuItemID() {
		String sql = "select max(itemid) from menu";
		return jdbcTemplate.queryForObject(sql, new Object[] {}, long.class) + 1; 
	}
	
	public long createOrderID(long customerId) {
		System.out.println("About to create Customer Id");
		/*
		 * Get MAX order ID
		 * create var MAX + 1
		 * 
		 */
		String sql = "select count(*) from menu";
		int  x = jdbcTemplate.query(sql, new intMapper()).get(0);
		if(x > 0) {
			sql = "select max(oid) from orders";
			int maxOid = jdbcTemplate.query(sql, new intMapper()).get(0);
			maxOid = maxOid + 1;
			if(!existingOrderId(maxOid)) {
				jdbcTemplate.execute("insert into customerorders (cid, oid, pay) values ("
						+ customerId + ", " + maxOid + ", 0)");
			}
			return maxOid;
		} else {
			
		}
		System.out.println("There are " + x + " elements in the table pizza");
		return x;
	}
	
	public ArrayList<Item> getMenu(long customerId) {
		System.out.println("Fetching Menu from the SHOP");
		
		String sql = "select * from menu"; 
		List<Item> menu = jdbcTemplate.query(sql, new ItemMapper());
		
		return (ArrayList<Item>) menu;
	}
	public boolean validUser(long cid) {
		String sql = "select count(*) from customerorders where cid = " + cid;
		int count = jdbcTemplate.query(sql, new intMapper()).get(0);
		if(count > 0) {
			return true;
		} else {
			return false;
		}
	}

	public Item addCartItem(Item newItem, long oid) {
		String isThere = "select count(*) from orders where oid = " + oid + 
				" and itemid = " + newItem.getItemID();
		if(jdbcTemplate.query(isThere, new intMapper()).get(0) == 0) {
			String sql = "insert into orders values(?,?,?,?)";
			jdbcTemplate.update(sql, new Object[] {
					oid, newItem.getItemID(), newItem.getAvailability(), newItem.getItemCost()
			});
			newItem.setName(getItemName(newItem.getItemID()));
			return newItem;
		} else {
			// write update(); query later
			Item item = new Item();
			updateCartItemCount(oid, newItem);
			return item;
		}
	}

	private String getItemName(int itemID) {
		String sql = "select name from menu where itemid  = " + itemID;
		return jdbcTemplate.queryForObject(sql, new Object[] {}, String.class);
	}
	public int getItemCost(int itemID) {
		String sql = "select cost from menu where itemid = " + itemID;
		int cost = jdbcTemplate.query(sql, new intMapper()).get(0);
		return cost;
	}

	public Item deleteCartItem(long oid, long itemid) {
		String sql = "select * from menu where itemid = " + itemid;
		if(!existingOrderId(oid)) System.out.println("Illegal! Not a valid order ID " + oid);
		List<Item> menu = jdbcTemplate.query(sql, new ItemMapper());
		sql = "delete from orders where oid = " + oid + 
				" and itemid = " + itemid;
		jdbcTemplate.execute(sql);
		return menu.get(0);
	}

	public Item updateCartItem(long oid, long itemid, Item item) {
		String sql = "update orders set itemcount = "+ item.getAvailability() + 
				" where oid = " + oid + " and itemid = " + itemid;
		jdbcTemplate.execute(sql);
		return item;
	}
	
	public ArrayList<Item> getCart(long oid) {
		String sql = "select * from orders where oid = " + oid;
		List<Item> menu = jdbcTemplate.query(sql, new cartMapper());
		return (ArrayList<Item>) menu; // why cast?
	}

	/* A D M I N     D A O */
	public Item addMenuItem(Item newItem) {
		long newItemID = getNewMenuItemID();
		String sql = "insert into menu values (" + newItemID + 
				", '" + newItem.getName() + "', " + newItem.getItemCost() + ", " + 
				newItem.getAvailability() + ")";
		jdbcTemplate.execute(sql);
		return getMenuItem(newItemID).get(0);
	}
	public Item updateMenuItem(Item newItem) {
		String takeCareOfStock = "";
		if(newItem.getAvailability() == 0) takeCareOfStock = "";
		else takeCareOfStock = ", stock = " + newItem.getAvailability();
		String sql = "update menu set cost = "+ newItem.getItemCost() + 
				takeCareOfStock + 
				" where itemid = " + newItem.getItemID();
		jdbcTemplate.execute(sql);
		return getMenuItem(newItem.getItemID()).get(0);
	}
	
	public Item deleteMenuItem(long itemID) {
		Item retItem = getMenuItem(itemID).get(0);
		String sql = "delete from menu where itemid = " + itemID;
		jdbcTemplate.execute(sql);
		return retItem;
	}

	/* U S E R   M A P P E R S */
	
	class intMapper implements RowMapper<Integer> {
		public Integer mapRow(ResultSet rs, int arg1) throws SQLException {
			int x = rs.getInt(1);
			return x;
		}
	}
	class cartMapper implements RowMapper<Item> {
		public Item mapRow(ResultSet rs, int arg1) throws SQLException {
			Item item = new Item();
			item.setItemID(rs.getInt("itemid"));
			String sql = "select name from menu where itemid = " + item.getItemID();
	
			String name = jdbcTemplate.queryForObject(sql, new Object[] {}, String.class);
			item.setName(name);
			sql = "select cost from menu where itemid = " + item.getItemID();
			int cost = jdbcTemplate.queryForObject(sql, new Object[] {}, Integer.class);
			item.setItemCost(cost);
			item.setAvailability(rs.getInt("itemcount"));
			return item;
		}
	}
	class ItemMapper implements RowMapper<Item> {
		public Item mapRow(ResultSet rs, int arg1) throws SQLException {
			Item item = new Item();
			item.setItemID(rs.getInt("itemid"));
			item.setName(rs.getString("name"));
			item.setItemCost(rs.getInt("cost"));
			item.setAvailability(rs.getInt("stock"));
			return item;
		}
	}
	class ItemMapper2 implements RowMapper<Item> {
		public Item mapRow(ResultSet rs, int arg1) throws SQLException {
			Item item = new Item();
			item.setName(rs.getString("name"));
			return item;
		}
	}
	public boolean existingOrderId(long oid) {
		String sql = "select count(*) from customerorders where oid = " + oid;
		long order = jdbcTemplate.queryForObject(sql, new Object[] {}, long.class);
		System.out.println("Order with orderID " + oid + " is " + order);
		if(order == 0) return false;
		else return true;
	}
	public int getCartAmount(long oid) {
		String sql = "select SUM(price * itemcount) from orders where oid = " + oid;
		return jdbcTemplate.query(sql, new intMapper()).get(0);
	}
	public boolean payCartAmount(long oid) {
		String sql = "update customerorders set pay = 1 where oid = " + oid;
		jdbcTemplate.execute(sql);
		return true;
	}
	public boolean payCartAmount2(long oid) {
		String sql = "update customerorders set pay = 1 where oid = " + oid;
		jdbcTemplate.execute(sql);
		return true;
	}
	public boolean paidFor(long oid) {
		String sql = "select pay from customerorders where oid = " + oid;
		if(jdbcTemplate.query(sql, new intMapper()).get(0) == 1) {
			System.out.println("Paid for oid " + oid);
			return true;
		}
		return false;
	}
	public void updateStock(long oid) {
		String sql = "update menu M set stock = (select M.stock - O.itemcount from orders O where O.oid = "+ oid + " and M.itemid = O.itemid) where itemid in (select itemid from orders where oid = " + oid + ")";
		jdbcTemplate.execute(sql);
	}
	public int getStock(int itemid) {
		return jdbcTemplate.query("select stock from menu where itemid = " + itemid, new intMapper()).get(0);
	}
	public boolean outOfStock(long oid) {
		System.out.println("In out of stock tester");
		for(Item i: getCart(oid)) {
			System.out.println("In stock = " + getStock(i.getItemID()) + " itemcount = " + i.getAvailability());
			if(getStock(i.getItemID()) - i.getAvailability() < 0) {
				System.out.println("Out of stock!");
				return true;
			}
		}
		return false;
	}
	public boolean validOidForCid(long oid, long cid) {
		String sql = "select count(*) from customerorders where cid = " + cid + " and oid = " + oid;
		if(jdbcTemplate.queryForObject(sql, Integer.class) == 1) {
			return true;
		}
		return false;
	}
	public boolean itemPresentInCart(long oid, int itemID) {
		String sql = "select count(*) from orders where oid = " + oid + " itemid = " + itemID;
		if(jdbcTemplate.queryForObject(sql, Integer.class) == 1) return true;
		return false;
	}
	public Item updateCartItemCount(long oid, Item item) {
		String sql = "update orders set itemcount = itemcount + " + item.getAvailability() + 
				" where itemid = " + item.getItemID() + " and oid = " + oid;
		return null;
	}

}
