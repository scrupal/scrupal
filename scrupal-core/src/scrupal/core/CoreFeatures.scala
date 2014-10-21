package scrupal.core

import scrupal.core.api.Feature

/** One line sentence description here.
  * Further description here.
  */
object CoreFeatures {

  /** Controls whether debug information is displayed at the bottom of page requests.
    * Debug data will also be tacked on to the end of JSON data delivered via the REST api in the "debug" field.
    */
  object DebugFooter extends Feature(Core, 'DebugFooter, "Show tables of debug information at bottom of each page.", false)

  /** Developer Mode Controls Some Aspects of Scrupal Functionality
    * The administrator of the site(s) might be a developer building a new module or extending Scrupal itself. For
    * such users, the developer mode can be set to relax some of Scrupal's security restrictions. Most notably when
    * DevMode is false and the site is not configured, every URL will take you to the configuration wizard. This may
    * not be convenient for developers, but saves a lot of confusion for end users as the site directs them towards
    * what they need to know next. :)
    */
  object DevMode extends Feature(Core, 'DeveloperMode, "Controls whether development mode facilities are enabled", true)

  /** Controls accessibility of the ConfigWizard
    * The ConfigWizard makes first time configuration easier but should not be enabled for production systems.
    * Disabling means web visitors will simply be redirected to the index if they attempt to use the configuration
    * urls.
    */
  object ConfigWizard extends Feature(Core, 'ConfigWizard, "Controls whether configuration by web request is allowed", true)

  /** Controls access to the REST API
    * Administrators may wish to turn off REST API access temporarily to ensure all clients are unable to transact
    * requests.
    */
  object RESTAPIAccess extends Feature(Core, 'RESTAPIAccess,
    "Allows access to entity instances via Scrupal's REST API")

  /** Controls access to the REST API Documentation
    * Administrators may wish to disable REST API Documentation
    */
  object RESTAPIDocumentation extends Feature(Core, 'RESTAPIDocumentation,
    "Allows access to auto-generated documentation for REST API entity instances.")

  object TopIndexPage extends Feature(Core, 'TopIndexPage,
    "Provides a Page entity to be displayed at the / path for a site.")

  /** Controls access to the One Page Application URLs
    * Administrators may wish to selectively disable the One Page Applications.
    */
  object OnePageApplications extends Feature(Core, 'OnePageApplications,
    "A feature that supports AngularJS based one page applications", false)


}

