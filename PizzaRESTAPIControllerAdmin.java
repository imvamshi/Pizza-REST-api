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
//import com.my.model.Order;

@RequestMapping("/admin")
@RestController
public class PizzaRESTAPIControllerAdmin {
	
	@Autowired
	PizzaDao pizzaService;
	
	/* ADMIN REST SERVICES */

	@RequestMapping(value = "/menu", 
			method = RequestMethod.GET, 
			produces = MediaType.APPLICATION_JSON_VALUE)
	public ArrayList<Item> showMenu() {
		ArrayList<Item> list = null;
		list = pizzaService.getMenu();
		return list;
	}
	@RequestMapping(value = "/menu", 
			method = RequestMethod.POST, 
			produces = MediaType.APPLICATION_JSON_VALUE)
	public Item addMenuItem(@RequestBody Item item) {
		return pizzaService.addMenuItem(item);
	}
	@RequestMapping(value = "/menu", 
			method = RequestMethod.PUT, 
			produces = MediaType.APPLICATION_JSON_VALUE)
	public Item updateMenuItem(@RequestBody Item item) {
		System.out.println("item things are = " + item.getItemID());
		return pizzaService.updateMenuItem(item);
	}
	@RequestMapping(value = "/menu/{itemID}", 
			method = RequestMethod.DELETE, 
			produces = MediaType.APPLICATION_JSON_VALUE)
	public Item deleteMenuItem(@PathVariable("itemID") long itemID) {
		return pizzaService.deleteMenuItem(itemID);
	}
	

}