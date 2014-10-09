'use strict';

describe('Controller: SideMenuCtrl', function () {

    // load the controller's module
    beforeEach(module('MSLImageExplorerApp'));

    var SideMenuCtrl, scope;

    var modal = {
        open: function(){
            var modalInstance = {
                shown: true, 
                dismiss: function(){
                    modalInstance.shown = false;
                }
            };
            console.log(modalInstance);
            return modalInstance;
        }
    };

    var marsPhotos = {
        instrumentList: {
            'fcam': {id: 'fcam', name: 'Chemcam RMI'},
            'mastcam_right': {id: 'mastcam_right', name: 'Right Mastcam'},
            'mastcam_left': {id: 'mastcam_left', name: 'Left Mastcam'},
            'mahli': {id: 'mahli', name: 'MAHLI'}
        }
    };
    
    var parentReloaded = false;

    // Initialize the controller and a mock scope
    beforeEach(function(){
        inject(function ($controller, $rootScope, $log) {
            scope = $rootScope.$new();
            scope.$parent.reload = function(){
                parentReloaded = true;
            };

            SideMenuCtrl = $controller('SideMenuCtrl', {
                $scope: scope,
                $log: $log,
                $modal: modal,
                MarsPhotosDBAccess: marsPhotos
            });
        });
    });

    it('should initialize the sidemenu at the first time the user opens', function () {
        expect(scope.isSidrInitialized).toBe(false);
        scope.openSidr();
        expect(scope.isSidrInitialized).toBe(true);
    });

    it('should show rover details modal window when asked', function(){
        scope.showRoverDetails();
        expect(scope.modalInstance.shown).toBe(true);
        expect(scope.showInstrumentList).toBe(true);
    });

    it('should set instrument to its parent view, dismiss modal window and reload its parent view', function(){
        scope.showRoverDetails();
        scope.setInstrument('fcam', {});
        expect(scope.$parent.instrument).toEqual('fcam');
        expect(scope.modalInstance.shown).toBe(false);
        expect(parentReloaded).toBe(true);
    });
});
