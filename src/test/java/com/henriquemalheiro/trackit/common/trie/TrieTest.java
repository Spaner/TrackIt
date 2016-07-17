/*
 * This file is part of Track It!.
 * Copyright (C) 2013 Henrique Malheiro
 * Copyright (C) 2015 Pedro Gomes
 * 
 * TrackIt! is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Track It! is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Track It!. If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package com.henriquemalheiro.trackit.common.trie;

import junit.framework.TestCase;

import org.junit.BeforeClass;
import org.junit.Test;

import com.henriquemalheiro.trackit.business.utility.trie.StringTrie;
import com.henriquemalheiro.trackit.business.utility.trie.Trie;

public class TrieTest extends TestCase {
    private static Trie<Product> productTrie = new StringTrie<Product>();

    @BeforeClass
    @Override
    public void setUp() throws Exception {
	productTrie.add("ham", new Product(1, "ham"));
	productTrie.add("hammer", new Product(2, "hammer"));
	productTrie.add("hammock", new Product(3, "hammock"));
	productTrie.add("ipod", new Product(4, "ipod"));
	productTrie.add("iphone", new Product(5, "iphone"));
    }

    @Test
    public void testAdd() {
	assertEquals(5, productTrie.size());
    }

    @Test
    public void testFind() {
	assertNotNull(productTrie.find("ham"));
    }

    @Test
    public void testSearch() {
	assertEquals(3, productTrie.search("ha").size());
    }

    @Test
    public void testContains() {
	assertEquals(true, productTrie.contains("ipod"));
    }

    @Test
    public void testGetAllKeys() {
	assertEquals(5, productTrie.getAllKeys().size());
    }
}

class Product {
    private int productId;
    private String productDesc;

    public Product(int productId, String productDesc) {
	this.productId = productId;
	this.productDesc = productDesc;
    }

    public int getProductId() {
	return productId;
    }

    public void setProductId(int productId) {
	this.productId = productId;
    }

    public String getProductDesc() {
	return productDesc;
    }

    public void setProductDesc(String productDesc) {
	this.productDesc = productDesc;
    }
}