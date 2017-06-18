<?xml version="1.0" encoding="ISO-8859-1" standalone="yes"?>
<CityModel xmlns="http://www.opengis.net/citygml/2.0" 
xmlns:xal="urn:oasis:names:tc:ciq:xsdschema:xAL:2.0" 
xmlns:gml="http://www.opengis.net/gml" 
xmlns:bldg="http://www.opengis.net/citygml/building/2.0" 
xmlns:gen="http://www.opengis.net/citygml/generics/2.0" 
xmlns:core="http://www.opengis.net/citygml/2.0" 
xmlns:xlink="http://www.w3.org/1999/xlink" 
xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
xsi:schemaLocation="http://www.opengis.net/citygml/building/2.0 http://schemas.opengis.net/citygml/building/2.0/building.xsd http://www.opengis.net/citygml/generics/2.0 http://schemas.opengis.net/citygml/generics/2.0/generics.xsd">
  <cityObjectMember>
    <bldg:Building gml:id="BUILDING">
	  <gml:boundedBy>
        <gml:Envelope srsName="" srsDimension="3">
		  <gml:lowerCorner>0.0 0.0 0.0</gml:lowerCorner>
          <gml:upperCorner>4.0 4.0 0.0</gml:upperCorner>
        </gml:Envelope>
      </gml:boundedBy>
      <core:creationDate>2017-05-26</core:creationDate>
	  
      <bldg:boundedBy>
        <bldg:GroundSurface gml:id="GROUND_SURFACE">
          <bldg:lod2MultiSurface>
            <gml:MultiSurface>
              <gml:surfaceMember>
                <gml:Polygon gml:id="POLYGON">
                  <gml:exterior>
                    <gml:LinearRing>
                      <gml:posList srsDimension="3">4.0 4.0 0.0 0.0 4.0 0.0 0.0 0.0 0.0 4.0 0.0 0.0 4.0 4.0 0.0</gml:posList>
                    </gml:LinearRing>
                  </gml:exterior>
				  <gml:interior>
                    <gml:LinearRing>
						<gml:pointProperty>
						   <gml:Point>						  
							  <gml:pos>3.0 3.0 0.0</gml:pos>
						   </gml:Point>
						</gml:pointProperty>
						<gml:pointProperty>
						   <gml:Point>						  
							  <gml:pos>1.0 3.0 0.0</gml:pos>
						   </gml:Point>
						</gml:pointProperty>
						<gml:pointProperty>
						   <gml:Point>						  
							  <gml:pos>1.0 1.0 0.0</gml:pos>
						   </gml:Point>
						</gml:pointProperty>
						<gml:pointProperty>
						   <gml:Point>						  
							  <gml:pos>2.0 1.0 0.0</gml:pos>
						   </gml:Point>
						</gml:pointProperty>
						<gml:pointProperty>
						   <gml:Point>						  
							  <gml:pos>2.0 2.0 0.0</gml:pos>
						   </gml:Point>
						</gml:pointProperty>
						<gml:pointProperty>
						   <gml:Point>						  
							  <gml:pos>3.0 2.0 0.0</gml:pos>
						   </gml:Point>
						</gml:pointProperty>
						<gml:pointProperty>
						   <gml:Point>						  
							  <gml:pos>3.0 3.0 0.0</gml:pos>
						   </gml:Point>
						</gml:pointProperty>
                    </gml:LinearRing>
                  </gml:interior>
                </gml:Polygon>
              </gml:surfaceMember>
            </gml:MultiSurface>
          </bldg:lod2MultiSurface>
        </bldg:GroundSurface>
      </bldg:boundedBy>	  
    </bldg:Building>
  </cityObjectMember>  
</CityModel>
