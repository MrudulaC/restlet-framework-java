/**
 * Copyright 2005-2013 Restlet S.A.S.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL
 * 1.0 (the "Licenses"). You can select the license that you prefer but you may
 * not use this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.test.ext.odata;

import java.util.Iterator;
import java.util.List;

import org.restlet.Component;
import org.restlet.data.Protocol;
import org.restlet.ext.odata.Query;
import org.restlet.test.RestletTestCase;
import org.restlet.test.ext.odata.cafe.Point;
import org.restlet.test.ext.odata.cafe.StructAny;
import org.restlet.test.ext.odata.cafecustofeeds.Cafe;
import org.restlet.test.ext.odata.cafecustofeeds.CafeCustoFeedsService;
import org.restlet.test.ext.odata.cafecustofeeds.Contact;
import org.restlet.test.ext.odata.cafecustofeeds.Item;

/**
 * Test case for OData customizable feeds.
 * 
 * @author Thierry Boileau
 * 
 */
public class ODataCafeCustoFeedsTestCase extends RestletTestCase {

    /** Inner component. */
    private Component component = new Component();

    /** OData service used for all tests. */
    private CafeCustoFeedsService service;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        component.getServers().add(Protocol.HTTP, 8111);
        component.getClients().add(Protocol.CLAP);

        component
                .getDefaultHost()
                .attach(
                        "/CafeCustoFeeds.svc",
                        new org.restlet.test.ext.odata.cafecustofeeds.CafeCustoFeedsApplication());

        component.start();
        service = new CafeCustoFeedsService();
    }

    @Override
    protected void tearDown() throws Exception {
        component.stop();
        component = null;
        service = null;
        super.tearDown();
    }

    /**
     * Tests the parsing of Feed element.
     */
    public void testQueryCafes() {
        Query<Cafe> query = service.createCafeQuery("/Cafes");
        Iterator<Cafe> iterator = query.iterator();

        assertTrue(iterator.hasNext());
        Cafe cafe = iterator.next();
        assertEquals("1", cafe.getId());
        assertEquals("Le Cafe Louis", cafe.getName());
        assertEquals("Cafe corp.", cafe.getCompanyName());
        assertEquals("Levallois-Perret", cafe.getCity());
        assertEquals(92300, cafe.getZipCode());

        assertNotNull(cafe.getSpatial());
        assertComplextTypeParsing(cafe.getSpatial());
        
        assertTrue(iterator.hasNext());
        cafe = iterator.next();
        assertEquals("2", cafe.getId());
        assertEquals("Le Petit Marly", cafe.getName());
        assertEquals("Cafe inc.", cafe.getCompanyName());
        assertEquals("Marly Le Roi", cafe.getCity());
        assertEquals(78310, cafe.getZipCode());
    }
    
    /**
     * This checks that if an entity has a property of complex type.
     * Also it checks for the collection properties of primitives as well as complex type.
     * 
     * @param point
     *            complex entity to test.
     */
    private void assertComplextTypeParsing(Point point) {
		
    	assertEquals("LINESTRING", point.getGeo_type());
    	assertEquals("GEONAME", point.getGeo_name());
    	
    	assertNotNull(point.getX());
    	assertTrue(point.getX().size()>0);
    	
    	List<java.lang.Double> listX = point.getX();
    	
    	for (Iterator iterator = listX.iterator(); iterator.hasNext();) {
			Double element = (Double) iterator.next();
			assertTrue(element instanceof Double);
			assertNotNull(element);
		}
    	
    	assertNotNull(point.getY());
    	assertTrue(point.getY().size()>0);
    	
    	List<java.lang.Double> listY = point.getY();
    	
    	for (Iterator iterator = listY.iterator(); iterator.hasNext();) {
			Double element = (Double) iterator.next();
			assertTrue(element instanceof Double);
			assertNotNull(element);
		}
    	
    	assertNotNull(point.getProperties());
    	assertTrue(point.getProperties().size()>0);
		
    	List<StructAny> listComplexObject = point.getProperties();
    	
    	for (Iterator iterator = listComplexObject.iterator(); iterator.hasNext();) {
			StructAny structAny = (StructAny) iterator.next();
			assertEquals("md", structAny.getName());
			assertEquals("FLOAT", structAny.getType());
			assertEquals("meters", structAny.getUnit());
			assertEquals("depth measure", structAny.getUnitType());
			assertEquals("[0.0,2670.9678]", structAny.getValues());
		}
	}

    /**
     * Tests the parsing of Feed element with expansion of the one to one
     * association "Contact".
     */
    public void testQueryCafesExpandContact() {
        Query<Cafe> query1 = service.createCafeQuery("/Cafes");
        Query<Cafe> query2 = query1.expand("Contact");

        Iterator<Cafe> iterator = query2.iterator();
        assertTrue(iterator.hasNext());
        Cafe cafe = iterator.next();
        assertEquals("1", cafe.getId());
        assertEquals("Le Cafe Louis", cafe.getName());
        assertEquals("Cafe corp.", cafe.getCompanyName());
        assertEquals("Levallois-Perret", cafe.getCity());
        assertEquals(92300, cafe.getZipCode());

        Contact contact = cafe.getContact();
        assertNotNull(contact);
        assertEquals("1", contact.getId());
        assertEquals("Agathe Zeblues", contact.getName());
        assertEquals("Chief", contact.getTitle());

        assertTrue(iterator.hasNext());
        cafe = iterator.next();
        assertEquals("2", cafe.getId());
        assertEquals("Le Petit Marly", cafe.getName());
        assertEquals("Cafe inc.", cafe.getCompanyName());
        assertEquals("Marly Le Roi", cafe.getCity());
        assertEquals(78310, cafe.getZipCode());

        contact = cafe.getContact();
        assertNotNull(contact);
        assertEquals("2", contact.getId());
        assertEquals("Alphonse Denltas", contact.getName());
        assertEquals("Boss", contact.getTitle());
    }

    /**
     * Tests the parsing of Feed element with expansion of the one to many
     * association "Items".
     */
    public void testQueryCafesExpandItem() {
        Query<Cafe> query1 = service.createCafeQuery("/Cafes");
        Query<Cafe> query2 = query1.expand("Items");

        Iterator<Cafe> iterator = query2.iterator();
        assertTrue(iterator.hasNext());
        Cafe cafe = iterator.next();
        assertEquals("1", cafe.getId());
        assertEquals("Le Cafe Louis", cafe.getName());
        assertEquals("Cafe corp.", cafe.getCompanyName());
        assertEquals("Levallois-Perret", cafe.getCity());
        assertEquals(92300, cafe.getZipCode());

        Iterator<Item> iterator2 = cafe.getItems().iterator();
        assertTrue(iterator2.hasNext());
        Item item = iterator2.next();
        assertEquals("1", item.getId());
        assertEquals("Poulet au curry", item.getDescription());

        assertTrue(iterator2.hasNext());
        item = iterator2.next();
        assertEquals("2", item.getId());
        assertEquals("Pate", item.getDescription());

        assertTrue(iterator.hasNext());
        cafe = iterator.next();
        assertEquals("2", cafe.getId());
        assertEquals("Le Petit Marly", cafe.getName());
        assertEquals("Cafe inc.", cafe.getCompanyName());
        assertEquals("Marly Le Roi", cafe.getCity());
        assertEquals(78310, cafe.getZipCode());

        iterator2 = cafe.getItems().iterator();
        assertTrue(iterator2.hasNext());
        item = iterator2.next();
        assertEquals("3", item.getId());
        assertEquals("Banana Split", item.getDescription());

        assertTrue(iterator2.hasNext());
        item = iterator2.next();
        assertEquals("4", item.getId());
        assertEquals("Cotes du Rhone", item.getDescription());
    }

    /**
     * Tests the parsing of Entry element.
     */
    public void testQueryContact() {
        Query<Contact> query = service.createContactQuery("/Contacts('1')");
        Iterator<Contact> iterator = query.iterator();
        assertTrue(iterator.hasNext());
        Contact contact = iterator.next();
        assertNotNull(contact);
        assertEquals("1", contact.getId());
        assertEquals("Agathe Zeblues", contact.getName());
        assertEquals("Chief", contact.getTitle());
    }

}
