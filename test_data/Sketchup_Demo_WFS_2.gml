<?xml version="1.0" encoding="ISO-8859-1" standalone="yes"?>
<CityModel xmlns="http://www.opengis.net/citygml/1.0"
	xmlns:xal="urn:oasis:names:tc:ciq:xsdschema:xAL:2.0" xmlns:gml="http://www.opengis.net/gml"
	xmlns:bldg="http://www.opengis.net/citygml/building/2.0" xmlns:gen="http://www.opengis.net/citygml/generics/2.0"
	xmlns:core="http://www.opengis.net/citygml/2.0" xmlns:xlink="http://www.w3.org/1999/xlink"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://www.opengis.net/citygml/building/2.0 http://schemas.opengis.net/citygml/building/2.0/building.xsd http://www.opengis.net/citygml/generics/2.0 http://schemas.opengis.net/citygml/generics/2.0/generics.xsd">
	<gml:name>EasyCityGMLTestcase</gml:name>
	<gml:boundedBy>
		<gml:Envelope srsName="urn:ogc:def:crs,crs:EPSG:27700,crs:EPSG:5101">
			<gml:pos srsDimension="3">536889.22 184689.99 5.124</gml:pos>
			<gml:pos srsDimension="3">536898.14 184696.2 12.072</gml:pos>
		</gml:Envelope>
	</gml:boundedBy>
	<cityObjectMember>
		<bldg:Building gml:id="Test_Building">
			<gml:boundedBy>
				<gml:Envelope srsName="urn:ogc:def:crs,crs:EPSG:27700,crs:EPSG:5101"
					srsDimension="3">
					<gml:lowerCorner>536889.22 184689.99 5.124</gml:lowerCorner>
					<gml:upperCorner>536898.14 184696.2 12.072</gml:upperCorner>
				</gml:Envelope>
			</gml:boundedBy>
			<core:creationDate>2016-02-22</core:creationDate>
			<core:externalReference>
				<core:informationSystem>https://www.ordnancesurvey.co.uk/
				</core:informationSystem>
				<core:externalObject>
					<core:name>1000001802553202</core:name>
				</core:externalObject>
			</core:externalReference>
			<bldg:roofType>3100</bldg:roofType>
			<bldg:measuredHeight uom="m">6.947</bldg:measuredHeight>
			<bldg:lod2Solid>
				<gml:Solid gml:id="UUID_4138bbdb-40dd-4e1c-92f0-c36d88f3c550">
					<gml:exterior>
						<gml:CompositeSurface gml:id="UUID_1a10e7c6-b738-47bf-8ba7-dff67d6630b7">
							<gml:surfaceMember
								xlink:href="#osgb1000001802553202_28c77d1f-8e20-44a8-8ded-fd56b79806ff_2_poly" />
							<gml:surfaceMember
								xlink:href="#osgb1000001802553202_a60a67dc-4e0d-4fc5-b6fb-cf5f48d01894_2_poly" />
							<gml:surfaceMember
								xlink:href="#osgb1000001802553202_2ddb6610-980c-43eb-8850-6dfd79210107_2_poly" />
							<gml:surfaceMember
								xlink:href="#osgb1000001802553202_36eaced1-e9ed-4930-8b36-2872035c5bd2_2_poly" />
							<gml:surfaceMember
								xlink:href="#osgb1000001802553202_e58c983b-462f-4eb5-9220-b03e9f4057fa_2_poly" />
							<gml:surfaceMember
								xlink:href="#osgb1000001802553202_74a292d0-529c-4e1e-bd39-e3b039ecbb3d_2_poly" />
							<gml:surfaceMember
								xlink:href="#osgb1000001802553202_29df4ec3-1f0d-4a43-aaa9-84c54f172801_2_poly" />
						</gml:CompositeSurface>
					</gml:exterior>
				</gml:Solid>
			</bldg:lod2Solid>
			
			<bldg:boundedBy>
				<bldg:WallSurface
					gml:id="osgb1000001802553202_74a292d0-529c-4e1e-bd39-e3b039ecbb3d_2">
					<gml:boundedBy>
						<gml:Envelope srsName="urn:ogc:def:crs,crs:EPSG:27700,crs:EPSG:5101"
							srsDimension="3">
							<gml:lowerCorner>536889.22 184694.69 5.124</gml:lowerCorner>
							<gml:upperCorner>536897.26 184696.2 12.072</gml:upperCorner>
						</gml:Envelope>
					</gml:boundedBy>
					<core:creationDate>2016-02-22</core:creationDate>
					<bldg:lod2MultiSurface>
						<gml:MultiSurface gml:id="UUID_d43783f7-f338-43d5-80e9-dfd7c728a68e">
							<gml:surfaceMember>
								<gml:Polygon
									gml:id="osgb1000001802553202_74a292d0-529c-4e1e-bd39-e3b039ecbb3d_2_poly">
									<gml:exterior>
										<gml:LinearRing
											gml:id="osgb1000001802553202_74a292d0-529c-4e1e-bd39-e3b039ecbb3d_2_poly_0_">
											<gml:posList srsDimension="3">
												536889.22 184694.69 5.124
												536889.22 184694.69 10.425
												536893.237 184695.444 12.072
												536897.26 184696.2 10.422
												536897.26 184696.2 5.124
												536889.22 184694.69 5.124
											</gml:posList>
										</gml:LinearRing>
									</gml:exterior>
								</gml:Polygon>
							</gml:surfaceMember>
						</gml:MultiSurface>
					</bldg:lod2MultiSurface>
				</bldg:WallSurface>
			</bldg:boundedBy>
			
			<bldg:boundedBy>
				<bldg:GroundSurface
					gml:id="osgb1000001802553202_29df4ec3-1f0d-4a43-aaa9-84c54f172801_2">
					<gml:boundedBy>
						<gml:Envelope srsName="urn:ogc:def:crs,crs:EPSG:27700,crs:EPSG:5101"
							srsDimension="3">
							<gml:lowerCorner>536889.22 184689.99 5.124</gml:lowerCorner>
							<gml:upperCorner>536898.14 184696.2 5.124</gml:upperCorner>
						</gml:Envelope>
					</gml:boundedBy>
					<core:creationDate>2016-02-22</core:creationDate>
					<bldg:lod2MultiSurface>
						<gml:MultiSurface gml:id="UUID_a6cc0604-b55c-45df-a5c3-3653c95efdca">
							<gml:surfaceMember>
								<gml:Polygon
									gml:id="osgb1000001802553202_29df4ec3-1f0d-4a43-aaa9-84c54f172801_2_poly">
									<gml:exterior>
										<gml:LinearRing
											gml:id="osgb1000001802553202_29df4ec3-1f0d-4a43-aaa9-84c54f172801_2_poly_0_">
											<gml:posList srsDimension="3">
												536889.22 184694.69 5.124
												536897.26 184696.2 5.124
												536898.14 184691.5 5.124
												536890.11 184689.99 5.124
												536889.22 184694.69 5.124
											</gml:posList>
										</gml:LinearRing>
									</gml:exterior>
								</gml:Polygon>
							</gml:surfaceMember>
						</gml:MultiSurface>
					</bldg:lod2MultiSurface>
				</bldg:GroundSurface>
			</bldg:boundedBy>
			
			<bldg:boundedBy>
				<bldg:WallSurface
					gml:id="osgb1000001802553202_36eaced1-e9ed-4930-8b36-2872035c5bd2_2">
					<gml:boundedBy>
						<gml:Envelope srsName="urn:ogc:def:crs,crs:EPSG:27700,crs:EPSG:5101"
							srsDimension="3">
							<gml:lowerCorner>536897.26 184691.5 5.124</gml:lowerCorner>
							<gml:upperCorner>536898.14 184696.2 10.426</gml:upperCorner>
						</gml:Envelope>
					</gml:boundedBy>
					<core:creationDate>2016-02-22</core:creationDate>
					<bldg:lod2MultiSurface>
						<gml:MultiSurface gml:id="UUID_d1a88af9-35ed-4dcb-8c10-bbb0d50e36de">
							<gml:surfaceMember>
								<gml:Polygon
									gml:id="osgb1000001802553202_36eaced1-e9ed-4930-8b36-2872035c5bd2_2_poly">
									<gml:exterior>
										<gml:LinearRing
											gml:id="osgb1000001802553202_36eaced1-e9ed-4930-8b36-2872035c5bd2_2_poly_0_">
											<gml:posList srsDimension="3">
												536897.26 184696.2 5.124
												536897.26 184696.2 10.422
												536898.14 184691.5 10.426
												536898.14 184691.5 5.124
												536897.26 184696.2 5.124
											</gml:posList>
										</gml:LinearRing>
									</gml:exterior>
								</gml:Polygon>
							</gml:surfaceMember>
						</gml:MultiSurface>
					</bldg:lod2MultiSurface>
				</bldg:WallSurface>
			</bldg:boundedBy>
			
			<bldg:boundedBy>
				<bldg:WallSurface
					gml:id="osgb1000001802553202_e58c983b-462f-4eb5-9220-b03e9f4057fa_2">
					<gml:boundedBy>
						<gml:Envelope srsName="urn:ogc:def:crs,crs:EPSG:27700,crs:EPSG:5101"
							srsDimension="3">
							<gml:lowerCorner>536890.11 184689.99 5.124</gml:lowerCorner>
							<gml:upperCorner>536898.14 184691.5 12.072</gml:upperCorner>
						</gml:Envelope>
					</gml:boundedBy>
					<core:creationDate>2016-02-22</core:creationDate>
					<bldg:lod2MultiSurface>
						<gml:MultiSurface gml:id="UUID_394ed612-9720-4619-8400-b05dc68858ee">
							<gml:surfaceMember>
								<gml:Polygon
									gml:id="osgb1000001802553202_e58c983b-462f-4eb5-9220-b03e9f4057fa_2_poly">
									<gml:exterior>
										<gml:LinearRing
											gml:id="osgb1000001802553202_e58c983b-462f-4eb5-9220-b03e9f4057fa_2_poly_0_">
											<gml:posList srsDimension="3">
												536898.14 184691.5 5.124
												536898.14 184691.5 10.426
												536894.127 184690.745 12.072
												536890.11 184689.99 10.425
												536890.11 184689.99 5.124
												536898.14 184691.5 5.124
											</gml:posList>
										</gml:LinearRing>
									</gml:exterior>
								</gml:Polygon>
							</gml:surfaceMember>
						</gml:MultiSurface>
					</bldg:lod2MultiSurface>
				</bldg:WallSurface>
			</bldg:boundedBy>
			
			<bldg:boundedBy>
				<bldg:WallSurface
					gml:id="osgb1000001802553202_2ddb6610-980c-43eb-8850-6dfd79210107_2">
					<gml:boundedBy>
						<gml:Envelope srsName="urn:ogc:def:crs,crs:EPSG:27700,crs:EPSG:5101"
							srsDimension="3">
							<gml:lowerCorner>536889.22 184689.99 5.124</gml:lowerCorner>
							<gml:upperCorner>536890.11 184694.69 10.425</gml:upperCorner>
						</gml:Envelope>
					</gml:boundedBy>
					<core:creationDate>2016-02-22</core:creationDate>
					<bldg:lod2MultiSurface>
						<gml:MultiSurface gml:id="UUID_932ce029-12ca-4643-97ef-8e20013982cd">
							<gml:surfaceMember>
								<gml:Polygon
									gml:id="osgb1000001802553202_2ddb6610-980c-43eb-8850-6dfd79210107_2_poly">
									<gml:exterior>
										<gml:LinearRing
											gml:id="osgb1000001802553202_2ddb6610-980c-43eb-8850-6dfd79210107_2_poly_0_">
											<gml:posList srsDimension="3">
												536890.11 184689.99 5.124
												536890.11 184689.99 10.425
												536889.22 184694.69 10.425
												536889.22 184694.69 5.124
												536890.11 184689.99 5.124
											</gml:posList>
										</gml:LinearRing>
									</gml:exterior>
								</gml:Polygon>
							</gml:surfaceMember>
						</gml:MultiSurface>
					</bldg:lod2MultiSurface>
				</bldg:WallSurface>
			</bldg:boundedBy>
			
			<bldg:boundedBy>
				<bldg:RoofSurface
					gml:id="osgb1000001802553202_28c77d1f-8e20-44a8-8ded-fd56b79806ff_2">
					<gml:boundedBy>
						<gml:Envelope srsName="urn:ogc:def:crs,crs:EPSG:27700,crs:EPSG:5101"
							srsDimension="3">
							<gml:lowerCorner>536889.22 184689.99 10.425</gml:lowerCorner>
							<gml:upperCorner>536894.127 184695.444 12.072</gml:upperCorner>
						</gml:Envelope>
					</gml:boundedBy>
					<core:creationDate>2016-02-22</core:creationDate>
					<bldg:lod2MultiSurface>
						<gml:MultiSurface gml:id="UUID_1b7fcdd3-f0d3-4934-93b5-cce7d008ebd9">
							<gml:surfaceMember>
								<gml:Polygon
									gml:id="osgb1000001802553202_28c77d1f-8e20-44a8-8ded-fd56b79806ff_2_poly">
									<gml:exterior>
										<gml:LinearRing
											gml:id="osgb1000001802553202_28c77d1f-8e20-44a8-8ded-fd56b79806ff_2_poly_0_">
											<gml:posList srsDimension="3">
												536890.11 184689.99 10.425
												536894.127 184690.745 12.072
												536893.237 184695.444 12.072
												536889.22 184694.69 10.425
												536890.11 184689.99 10.425
											</gml:posList>
										</gml:LinearRing>
									</gml:exterior>
								</gml:Polygon>
							</gml:surfaceMember>
						</gml:MultiSurface>
					</bldg:lod2MultiSurface>
				</bldg:RoofSurface>
			</bldg:boundedBy>
			
			<bldg:boundedBy>
				<bldg:RoofSurface
					gml:id="osgb1000001802553202_a60a67dc-4e0d-4fc5-b6fb-cf5f48d01894_2">
					<gml:boundedBy>
						<gml:Envelope srsName="urn:ogc:def:crs,crs:EPSG:27700,crs:EPSG:5101"
							srsDimension="3">
							<gml:lowerCorner>536893.237 184690.745 10.422</gml:lowerCorner>
							<gml:upperCorner>536898.14 184696.2 12.072</gml:upperCorner>
						</gml:Envelope>
					</gml:boundedBy>
					<core:creationDate>2016-02-22</core:creationDate>
					<bldg:lod2MultiSurface>
						<gml:MultiSurface gml:id="UUID_93f5931e-3405-49f9-bee2-f80731871918">
							<gml:surfaceMember>
								<gml:Polygon
									gml:id="osgb1000001802553202_a60a67dc-4e0d-4fc5-b6fb-cf5f48d01894_2_poly">
									<gml:exterior>
										<gml:LinearRing
											gml:id="osgb1000001802553202_a60a67dc-4e0d-4fc5-b6fb-cf5f48d01894_2_poly_0_">
											<gml:posList srsDimension="3">
												536894.127 184690.745 12.072
												536898.14 184691.5 10.426
												536897.26 184696.2 10.422
												536893.237 184695.444 12.072
												536894.127 184690.745 12.072
											</gml:posList>
										</gml:LinearRing>
									</gml:exterior>
								</gml:Polygon>
							</gml:surfaceMember>
						</gml:MultiSurface>
					</bldg:lod2MultiSurface>
				</bldg:RoofSurface>
			</bldg:boundedBy>
			<bldg:address>
				<core:Address gml:id="UUID_803c7dc7-f911-41d9-bfe5-770cee496af4">
					<core:xalAddress>
						<xal:AddressDetails>
							<xal:Country>
								<xal:CountryName>United Kingdom</xal:CountryName>
								<xal:Locality Type="Town">
									<xal:LocalityName>London</xal:LocalityName>
									<xal:Thoroughfare Type="Street">
										<xal:ThoroughfareNumber>7</xal:ThoroughfareNumber>
										<xal:ThoroughfareName>GAINSBOROUGH STREET
										</xal:ThoroughfareName>
									</xal:Thoroughfare>
									<xal:PostalCode>
										<xal:PostalCodeNumber>E9 5GY</xal:PostalCodeNumber>
									</xal:PostalCode>
								</xal:Locality>
							</xal:Country>
						</xal:AddressDetails>
					</core:xalAddress>
				</core:Address>
			</bldg:address>
		</bldg:Building>
	</cityObjectMember>
</CityModel>
