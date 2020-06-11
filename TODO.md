# Things to Do

### Usability

* Implement command-line interface (and some amount of documentation).


### Simulation Representation

* Item database, flesh out `data/items.json` to include a small variety of weapons and simplify input files.
    * Damage should have penetration values
    * Las weapons should have overload and overcharge settings costing extra ammo
    * One-handed vs two-handed weapons?
* Effects should have degrees (i.e. Primitive(7))
* Handle when units have more than one weapon
* Actions that take more than one full action (i.e. reloading a stub revolver)


### Simulation Computation

* Ranged attacks
    * Penetration
    * Consider range
    * Can jam the weapon, requiring unjamming and reloading
    * Reduce ammo on firing unit, remove actions that cannot be taken if ammunition is too low
    * Considers cover(?)
* Repeated simulations: re-seed things and try again X times to produce a % nuance to results
* Psyker stuff(?)


### Tactical Decisionmaking

* `TacticalActionStrategy` considers actions to take during a turn deciding between
    * `RangedAttackStrategy` when range is good and ammo is available
    * `MeleeAttackStrategy` when ranged attack is not possible or main weapon is melee
    * ... and others as appropriate.
    
### Deferred / Low-Priority

* Targeted actions should generate their own attribute modifiers based on the target (short range etc.)
* Weapon jams
    * Remove all weapon actions except unjam, empties ammo
* Damage types