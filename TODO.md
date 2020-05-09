# Things to Do

### Usability

* Implement command-line interface (and some amount of documentation).


### Simulation Representation

* Item database, flesh out `data/items.json` to include a small variety of weapons and simplify input files.
    * Damage should have penetration values
    * Las weapons should have overload and overcharge settings costing extra ammo
* Effects should have degrees (i.e. Primitive(7))


### Simulation Computation

* Turn simulation: units actually take actions
* Ranged attacks
    * Consider range
    * Roll hits
    * Consider enemy unit damage reduction on appropriate body parts
    * Apply damage to enemy unit
    * Reduce ammo on firing unit
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