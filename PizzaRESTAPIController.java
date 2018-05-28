package com.my.controller;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.my.dao.PizzaDao;
import com.my.model.Item;

@RestController
public class PizzaRESTAPIController {
	@Autowired
	PizzaDao pizzaService;
	
	/* Requestmapping is used to map HTTP request of getting menu as {custID}/users */
	// Don't show menu if custID is fake
	@RequestMapping(value = "/{custID}/menu", 
			method = RequestMethod.GET, 
			produces = MediaType.APPLICATION_JSON_VALUE)
	public ArrayList<Item> showMenuToCustomer(@PathVariable("custID") long cid) {
		ArrayList<Item> list = null;
		if(pizzaService.validUser(cid)) {
			list = pizzaService.getMenu();
			return list;
		} 
		list = new ArrayList<>();
		Item itm = new Item();itm.setItemID(0);
		itm.setName("INVALID USER, Restricted!");list.add(itm);
		return list;
	}
	//create order
	@RequestMapping(value = "/{custID}/createorder",
			method = RequestMethod.POST,
			produces = MediaType.APPLICATION_JSON_VALUE)
	public long getNewOrderID(@PathVariable("custID") long cid) {
		long Oid = pizzaService.createOrderID(cid);
		return Oid;
	}
	//Adding item to cart
	@RequestMapping(value = "/{custID}/orders/{orderID}",
			method = RequestMethod.POST,
			produces = MediaType.APPLICATION_JSON_VALUE)
	public Item addCartItem(@PathVariable("custID") long cid,
			@PathVariable("orderID") long oid,
			@RequestBody Item item) {
		if(pizzaService.paidFor(oid)) {
			return null;
		}
		// itemID, itemCOUNT, itemPRICE
		Item newItem = new Item();
		if(!pizzaService.existingOrderId(oid)) {
			System.out.println("not Adding an Item to orderID = " + oid);
			return null;
		}
		/*if(pizzaService.itemPresentInCart(oid, item.getItemID())) {
			return pizzaService.updateCartItemCount(oid, item);
		}*/
		newItem.setItemID(item.getItemID());
		int thenItemCost = pizzaService.getItemCost(newItem.getItemID());
		newItem.setItemCost(thenItemCost);
		
		if(item.getAvailability() == 0) {
			newItem.setAvailability(1);
		} else {
			newItem.setAvailability(item.getAvailability());
		}
		return pizzaService.addCartItem(newItem, oid);
	}
	//DELETE  FROM  CART
	@RequestMapping(value = "{custID}/orders/{orderId}/{itemId}",
			method = RequestMethod.DELETE,
			produces = MediaType.APPLICATION_JSON_VALUE)
	public Item deleteCartItem(@PathVariable("custID") long cid,
			@PathVariable("orderId") long oid,
			@PathVariable("itemId") long itemid) {
		if(pizzaService.paidFor(oid)) {
			return null;
		}
		return pizzaService.deleteCartItem(oid, itemid);
	}
	// UPDATE  FROM  CART
	@RequestMapping(value = "{custID}/orders/{orderId}/{itemId}",
			method = RequestMethod.PUT,
			produces = MediaType.APPLICATION_JSON_VALUE)
	public Item updateCartItem(@PathVariable("custID") long cid,
			@PathVariable("orderId") long oid,
			@PathVariable("itemId") long itemid,
			@RequestBody Item item) {
		System.out.println("Updating the cart");
		if(pizzaService.paidFor(oid)) {
			return null;
		}
		return pizzaService.updateCartItem(oid, itemid, item);
	}
	// show items in the cart 
	@RequestMapping(value = "{custID}/orders/{orderId}",
			method = RequestMethod.GET,
			produces = MediaType.APPLICATION_JSON_VALUE)
	public ArrayList<Item> displayCart(@PathVariable("custID") long cid,
			@PathVariable("orderId") long oid) {
		if(!pizzaService.validOidForCid(oid, cid)) {
			return null;
		}
		return pizzaService.getCart(oid);
	}
	
	@RequestMapping(value = "{custID}/orders/{orderId}/bill",
			method = RequestMethod.GET,
			produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public String displayCartBill(@PathVariable("custID") long cid,
			@PathVariable("orderId") long oid) {
		if(!pizzaService.validOidForCid(oid, cid)) {
			return "Nah Thats not your cart. You are not supposed to peep into others cart!";
		}
		return Integer.toString(pizzaService.getCartAmount(oid));
	}
	
	
	// SHOW  TOTAL  AMOUNT CONFIRM ORDER
	@RequestMapping(value = "{custID}/orders/{orderId}/confirm",
			method = RequestMethod.POST,
			produces = MediaType.APPLICATION_JSON_VALUE)
	public String checkout(@PathVariable("custID") long cid,
			@PathVariable("orderId") long oid) {
		if(!pizzaService.validOidForCid(oid, cid)) {
			return "So nice of you. But, you are not supposed to pay for others!";
		}
		if(pizzaService.outOfStock(oid) && !pizzaService.paidFor(oid)) {
			return "Remove some items and try. We ran out of stock";
		} else if(!pizzaService.paidFor(oid)) {
			int amount = pizzaService.getCartAmount(oid);
			System.out.println("Cart amount is " + amount);
			boolean pay = pizzaService.payCartAmount(oid);
			pizzaService.updateStock(oid);
			String receipt = "Order has been placed. Order id:" + oid + ". Amount paid " + amount;
			return receipt;
		} else {
			return "Already paid.";
		}
	}
	// CANCEL  ORDER. Quite complex. write later
	/*@RequestMapping(value = "{custID}/orders/{orderId}/confirm",
			method = RequestMetho	d.POST,
			produces = MediaType.APPLICATION_JSON_VALUE)
	public String cancelOrder(@PathVariable("custID") long cid,
			@PathVariable("orderId") long oid) {
		if(pizzaService.outOfStock(oid)) {
			return "Remove some items and try. We ran out of stock";
		} else if(!pizzaService.paidFor(oid)) {
			int amount = pizzaService.getCartAmount(oid);
			System.out.println("Cart amount is " + amount);
			boolean pay = pizzaService.payCartAmount(oid);
			pizzaService.updateStock(oid);
			String receipt = "Order has been placed. Order id:" + oid + ". Amount paid " + amount;
			return receipt;
		} else {
			return "Already paid";
		}
	} */
	
	/* ADMIN REST SERVICES */

}
