(function () {
    angular.module('common').service('gisHelper', function () {

        this.dataProjectionCode = 'EPSG:4326';
        this.mapProjectionCode = 'EPSG:3857';

        this.getFeaturesFromZones = function (zones, format) {
            var _this = this;
            var features = new ol.Collection();

            zones.forEach(function (zone) {
                    if (zone.polygon !== null) {
                        features.push(_this.convertZoneToFeature(zone, format));
                    }
                }
            );

            return features;
        };

        this.getFeatureForZone = function (zone, vectorLayer) {
            return vectorLayer.getSource().getFeatureById(zone.id);
        };

        this.convertZoneToFeature = function (zone, format) {
            var feature = format.readFeature(zone.polygon, {
                dataProjection: this.dataProjectionCode,
                featureProjection:this.mapProjectionCode
            });

            feature.setId(zone.id);

            return feature;
        };

        this.convertFeatureToWKT = function (feature, format) {

            return format.writeFeature(feature, {
                dataProjection: this.dataProjectionCode,
                featureProjection:this.mapProjectionCode
            });
        };

        this.convertPositionToCoordinate = function (position) {
            return ol.proj.transform([position.lon, position.lat], this.dataProjectionCode,this.mapProjectionCode);
        };

        this.convertRouteToCoordinates = function (route) {
            var _this = this;
            var coordinates = route.map(function (wayPoint) {
                    return _this.convertPositionToCoordinate(wayPoint.position)
                }
            );
            return coordinates;
        };


        this.getZoneStyle = function (zone) {
            var styleForZoneType;

            var zoneType = zone.type;

            if (zoneType === 'OrderZone') {
                styleForZoneType = new ol.style.Style({
                    stroke: new ol.style.Stroke({
                        color: 'rgba(0, 192, 239, 1.0)',
                        width: 3
                    }),
                    fill: null
                });
            } else if (zoneType === 'FlightZone') {
                styleForZoneType = new ol.style.Style({
                    stroke: null,
                    fill: new ol.style.Fill({
                        color: 'rgba(0, 28, 247, 0.8)'
                    })
                });
            } else if (zoneType === 'DeliveryZone') {
                styleForZoneType = new ol.style.Style({
                    stroke: null,
                    fill: new ol.style.Fill({
                        color: 'rgba(0, 166, 90, 0.8)'
                    })
                });
            } else if (zoneType === 'LoadingZone') {
                styleForZoneType = new ol.style.Style({
                    stroke: null,
                    fill: new ol.style.Fill({
                        color: 'rgba(243, 156, 18, 0.8)'
                    })
                });
            }
            return styleForZoneType;
        };

        this.getZoneById = function (zones, zoneId) {
            return zones.filter(function (zone) {
                return zone.id === zoneId;
            })[0];
        }

    })
}());

