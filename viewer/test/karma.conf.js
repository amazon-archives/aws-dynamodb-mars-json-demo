// Karma configuration
// http://karma-runner.github.io/0.12/config/configuration-file.html
// Generated on 2014-08-19 using
// generator-karma 0.8.3

module.exports = function(config) {
  'use strict';

  config.set({
    // enable / disable watching file and executing tests whenever any file changes
    autoWatch: true,

    // base path, that will be used to resolve files and exclude
    basePath: '../',

    // testing framework to use (jasmine/mocha/qunit/...)
    frameworks: ['jasmine'],

    preprocessors: {
      '**/*.html': ['ng-html2js']
    },

    // list of files / patterns to load in the browser
    files: [
       // bower:js
       'bower_components/jquery/dist/jquery.js',
       'bower_components/es5-shim/es5-shim.js',
       'bower_components/aws-sdk/dist/aws-sdk.js',
       'bower_components/angular/angular.js',
       'bower_components/json3/lib/json3.js',
       'bower_components/angular-resource/angular-resource.js',
       'bower_components/angular-sanitize/angular-sanitize.js',
       'bower_components/angular-touch/angular-touch.js',
       'bower_components/angular-route/angular-route.js',
       'bower_components/angular-bootstrap/ui-bootstrap-tpls.js',
       'bower_components/sidr/jquery.sidr.min.js',
       'bower_components/bootstrap/dist/js/bootstrap.js',
       'bower_components/bootstrap-datepicker/js/bootstrap-datepicker.js',
       'bower_components/blueimp-gallery/js/blueimp-helper.js',
       'bower_components/blueimp-gallery/js/blueimp-gallery.js',
       'bower_components/blueimp-gallery/js/blueimp-gallery-fullscreen.js',
       'bower_components/blueimp-gallery/js/blueimp-gallery-indicator.js',
       'bower_components/blueimp-gallery/js/blueimp-gallery-video.js',
       'bower_components/blueimp-gallery/js/blueimp-gallery-vimeo.js',
       'bower_components/blueimp-gallery/js/blueimp-gallery-youtube.js',
       'bower_components/blueimp-bootstrap-image-gallery/js/bootstrap-image-gallery.js',
       'bower_components/moment/moment.js',
       'bower_components/angular-mocks/angular-mocks.js',
       // endbower
       'app/scripts/**/*.js',
       '.tmp/scripts/**/*.js',
       'test/spec/**/*.js',
       'dist/**/*.html'
    ],

    // list of files / patterns to exclude
    exclude: [
    'node_modules/**/*.html',
    'bower_components/**/*.html'
    ],

    // web server port
    port: 8080,

    // Start these browsers, currently available:
    // - Chrome
    // - ChromeCanary
    // - Firefox
    // - Opera
    // - Safari (only Mac)
    // - PhantomJS
    // - IE (only Windows)
    browsers: [
      'PhantomJS'
    ],

    // Which plugins to enable
    plugins: [
      'karma-phantomjs-launcher',
      'karma-ng-html2js-preprocessor',
      'karma-jasmine'
    ],

    // Continuous Integration mode
    // if true, it capture browsers, run tests and exit
    singleRun: false,

    colors: true,

    // level of logging
    // possible values: LOG_DISABLE || LOG_ERROR || LOG_WARN || LOG_INFO || LOG_DEBUG
    logLevel: config.LOG_INFO,

    proxies: {
       '/dynamodb/': 'http://localhost:8000/'
    },

    ngHtml2JsPreprocessor: {
      cacheIdFromPath: function(filepath) {
        filepath = filepath.replace(/^dist\//, '');
        filepath = filepath.replace(/^app\//, '');        
        return filepath;
      }
    }
  });
};
