## Fixes/improvements

* [#3703] Fix potential packet memory leak.
* [#3726] Fix missing tooltip on Power Converter.
* [#3729] Fix potential crash when opening the manual.
* [#3748] Fix broken "Support de serveur" link in the French manual.
* Added a configuration option for network packet TTL. (Timothé GRISOT)
* Improved mod load times on certain platforms. (charagarland)
* Updated Chinese translation. (HfSr)
* Updated Unifont to 16.0.02.

## OpenOS fixes/improvements

* [#3714] Fix an OpenOS 1.8.0 regression causing event.pullFiltered() to effectively ignore filter timeouts.
* [#3727] Fix an exception handler bug in process.lua, uncovered by fixing recursive xpcall() handling in 1.8.4.

## List of contributors

asie, charagarland, DragDen, HfSr, Timothé GRISOT
