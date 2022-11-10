# Changelog

## [Update]
* feat: added staff uniforms and tools
* feat: double-sided text for hectometer boards
* feat: added several station and railroadcrossing signs

## [Fix]
* fix: render overlay issue
* potential fix: nbt desync (disappearing signals)

## [Fix]
* fix: missing language keys
* fix: mechanical wn signal models

## [Update]

* feat: added bue east signal
* feat: added more el signals
* feat: added more ne signals
* feat: added more other signals
* feat: added etcs signals
* feat: added ro signals
* feat: added ra11 dwarf signal
* feat: added tram signs
* feat: added railroad gates
* feat: added spring-loaded switch signals
* feat: added switch handles and mechanical wn signals
* feat: added hl block signals
* update: post block
* update: bue signs
* fix: no sounds on servers
* fix: semaphore signals with control box
* fix: ne signal heights

## [Fix]

* fix: crash on ressource pack loading

## [Update]

* feat: added new ModelRenderSystem
* feat: added semaphore signals
* feat: added andreas cross signals

## [Fix]

* fix: crash when signal is beeing destroyed
* fix: NPE with Redstone IO
* fix: Ra11 light was acitivated with train pathway

## [Fix]

* fix: partial pathways cannot be reset
* fix: partial pathways do not update correctly in UI

## [Update]

### BREAKING CHANGES!

* fix: several synchronization fixes
* feat: partly pathway resetting
* perf: reduced network load
* feat: auto recover from bad pathways
* perf: reduced redstone performance
* perf: only send needed data
* fix: improved dijkstra path detection
* feat: security patch for signal boxes (only one user at same time)
* fix: sh light signal status light
* feat: new sh light texture
* ref: optimize models

// Known issues:

* Custom names are sometimes not rendered
* Ra11 shows Ra12 with train drive path
* Edit not saved on exit (Workaround: Go to the Use tab before exiting)

## [Beta Update] 
* feat: Ra11 added to signalbox
* feat: rename signal via right click
* serveral fixes with ZS3 and ZS3_PlATE

  **Warning: Contains a lot of known issues that will be fixed with coming versions.**

## [Beta Update] 08.05.2022
* feat: ZS3_PLATE intigrated to Interoperability
* fix: various fixes
* fix: Missing KS on shunting path
* fix: KS distant with HV
* fix: Hl distant with HV
* fix: Vr with KS

## [Internal Update]

* deps: Updatet gradle wrapper and forge gradle

## [Beta Update] 05.06.2022
* feat: new HL-Signal "HL Exit"
* feat: Signalinteroperability between all signalsystems
* fix: several issues

## [Beta Update]
* fix: Missing language keys
* ref: Internal refactoring
* ci: Code checks
* fix: signal config for signalbox
* fix: desync issues
* fix: serveral issues

## [Beta Update]

* feat: Added UIs for the redstone io blocks and rename possibility
* feat: Added automatic reset of pathways
* feat: Signal Box blocks now show pathways currently in use in red

### GUI Lib

* fix: Issue with text not being shown correctly after layout calculation
* fix: Potential issue with delayed send on an invalid player
* fix: Removed debugging

## [Beta Update]

* fix: Update UI Lib to fix packet crashing
* opt: Reduced network load through compression

## [Beta Update]

* fix: Distant signals not updating
* fix: Distant signals not reseting if pathway is reset

## [Beta Update] 03.28.2022

* fix: Update issue with equals method
* feat: Signals automatically reset on link
* feat: Signals fall back to danger if signalbox is reset
* feat: Signals can now be individually removed
* feat: Signal names or type now show up while configuring
* feat: Terminus symbols can now be used as pathway destinations
* feat: Added icons for better visibility in settings page
* fix: Added paging in config
* feat: Added button for better usage in signal box
* feat: Selection of pages is now visible
* fix: Fixed synchronization issue after removing a tiles content
* feat: Added shunting pathways (SH Signals) implementation

## [Alpha Update] 03.20.2022

* Added Redstone output (e.g. switch control)
* Fixed several issues
* Signal of previous path way updated accordingly

  **This update is an alpha version and does not contain fully functional signal boxes. We ask you to test the system and give us feedback on functionality, bugs and features.**

## [Internal] 03.12.2022 - 2

* Internal Open Computers update

## [Internal] 03.12.2022 - 1

* Internal Forge version update

## [Alpha update] 02.22.2022 - 1

* Fixed correct signal aspect for hp signals
* Train routes now only applied between neighbor signals in the right direction :TM:
* Train routes can now be reset manually
* Fixed backend issues
* Fixed issue with container closed
* Fixed sync issue
* Added redstone io blocks for the signal box
* Improvements and fixes

  **This update is an alpha version and does not contain fully functional signal boxes. We ask you to test the system and give us feedback on functionality, bugs and features.**

## [Alpha update] 02.16.2022 - 1

* Added signal box

  **This update is an alpha version and does not contain fully functional signal boxes. We ask you to test the system and give us feedback on functionality, bugs and features.**

## [Update] 02.04.2022 - 1

* Imgbot optimizations

## [Update] 02.01.2022 - 1

* Readded new redstone mode interface
* Updated readme
* Added yellow-red traffic light state

## [Fix] 01.30.2022 - 1

* Fixed client sync 

## [Update/Fixes] 01.28.2022 - 1

* Updated UI system
* Updated synchronization system
* Temporarily removed Redstone mode
* Fixed various bugfixes
* Fixed HV models

## [Addition/Fix] 01.15.2022 - 1

* Fixed missing Ne2 shield for H/V signals
* Added SH status light
* Added Ra11b shunting signal
* Added Zs2v for HL and KS signals

## [Fix] 01.10.2022 - 1

* Fixed signal turning

## [Addition] 12.22.2021 - 1

* Added more HV and KS signal shields
* Added more tram signals
* Added post block

## [Addition] 11.25.2021 - 1

* Added more numbers for station number

## [Addition] 10.30.2021 - 1

* Added ZS plates for HV and KS signals

## [Fix] 9.22.2021 - 1

* Fixed Wn signal

## [Update] 8.30.2021 - 1

* Better Linking between parts
* Added linkable API

## [Addition/Fixes/Update] 8.27.2021 - 1

* Added config values for the light levels of blocks
* Reduced amount of textures
* Added emissive textures for Optifine
* Added arrow signs for NE signals
* Added station name signs

## [Minor update] 8.18.2021 - 1

* Optimized images

## [Fixes/Update] 8.10.2021 - 1

* Added additional localized information
* Fixed issue with gui size
* Streamlined UI System to give a more uniform look
* Fixed Bü4 model
* Update DE and EN language

## [Fixes/Update/Addition] 8.02.2021 - 1

* Fixed some texture flickering
* Removed unused models
* Updated SH Signal
* Added WN Signals
* Added Ra Signal (Hump Shunting Signal)
* Added ZP8 and ZP9
* Add dependency system
* Fixed issue with gui stage not being shown in renderer for controller
* Fixed signals

## [Fixes/Update/Additions] 7.31.2021 - 1

* Fixed issue with BU Lightsignal that could be without the actual signal
* Fixed issue with crash on old worlds
* Removed confusing off states
* Removed unused states
* Fixed issues with Changeable Stage
* Updated internal rendering for name renderer
* Fixed issue with height on RA signals
* In GUI renderer now shows custom name plate
* Added BUE2, BUE3 Signs
* Added Station Number Sign
* Added more ZS3 Textures
* Compacted RS Signal options

  **A lot was done to remove blank states that could confuse the user. This however could possible break some states, so you might need to reset some signals. Impacted Signals are (but not limited to): Tram Signals, RS Signals, BUE Light Signals, RA Signals, SH Signal**

## [Fixed] 7.30.2021 - 1

* Fixed issue with server client sync on redstone single mode

## [Fixed] 7.28.2021 - 2

* Fixed issue with startup

## [Hotfix] 7.28.2021 - 1

* Fixed an issue while block loading!

## [Addition] 7.24.2021 - 1

* Added Multiplex Modus to Signal Controller
(You maybe need to study IT to use it tho)

## [Update/Fixes] 7.18.2021 - 3

* Updated Controller Texture
* Updated Lang Keys
* Fixed overlapping text
* Fixed some text formatting
* Updated information display
* Fixed changelog

## [Release] 7.18.2021 - 2

First release! (Removed debugging!)

## [Fix] 7.18.2021 - 1

* Fixed version not correctly used and shown by the forge system

## [Update/Fixes] 7.15.2021 - 1

* Fixed quad color not showing in UI
* Streamlined ZS3(v)/ZS2 renderer for better performance

## [Additions] 7.11.2021 - 1

* Added preview of redstone mode
* Controller can now be customized to allow for it to be controlled via redstone

## [Fix] 6.25.2021 - 1

* Fixed potential NPE in renderer

## [Update/Fixes] 6.23.2021 - 1

* Updated UI for our signal controller
* Fixed issues with UI usage when the signal is in an unloaded chunk
* Fixed Sync issues
* Added visual feedback for changes in UI
* Fixed some properties

## [Update] 6.10.2021 - 1

* Updated UI to a more usable standard

## [Additions] 6.9.2021 - 1

* Added NE5 sign

## [Internal changes] 6.8.2021 - 1

* Model optimization

## [Additions] 6.7.2021 - 1

* Added Hectometer

## [Internal changes] 4.18.2021 - 2

* Fixed textures and particles
* NE Signs texture cleanup

## [Additions] 4.18.2021 - 1

* Added NE signs

## [Internal changes] 4.10.2021 - 1

* Texture Cleanup

## [Fixes] 4.5.2021 - 2

* Fixed crash during startup

## [Major additions] 4.5.2021 - 1

* Added RA signs
* Added BUE signs
* Added EL signs
* Added SH signs
* Added other signs
* Fixed issue with block pick
* Fixed issue with placement tools

## [Additions] 4.4.2021 - 3

* Added naming for SH signals
* Fixed issues with placement tool

## [Fixes] 4.4.2021 - 2

* Fixed issue with signal controlling out of loaded chunks
* Fixed issue with block pick returning null

## [Internal changes] 4.4.2021 - 1

## [Additions] 3.3.2021 - 1

* Added LF Signs aswell as a new placement tool
* Added new coloring API
* Signs can not be linked to a controller

## [Fixes] 19.02.2021 - 1

* Fixed issue with placement tool leading to game crash

## [Backend] 14.02.2021 - 1

* Backend changes
* Fixed issue with Controller GUI
* Fixed and other Render height issue

## [Fixes] 13.02.2021 - 1

* Fixed text renderer issue
* Fixed Bounding Box height
* Backend changes

## [Fixes] 12.02.2021 - 1

* Fixed desync issue with controller

## 1.0

Inital Curserlease
