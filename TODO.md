# Things to Do

### Simulation Representation

* Flesh out and implement how weapons change unit attributes, and the various options that can be selected and exposed to tactical decision-making (rate of fire, multiple weapons, improvised melee)
* Add a proper co-ordinate system so that we can robustly handle units advancing or retreating from melee
* Handle weapon ranges

### Simulation Computation

* Turn simulation: units actually take actions
* Attacks
    * Consider range
    * Roll hits
    * Consider enemy unit damage reduction on appropriate body parts
    * Apply damage to enemy unit
    * Reduce ammo on firing unit
    * Considers cover(?)
* Repeated simulations: re-seed things and try again X times to produce a % nuance to results
* Psyker stuff(?)

### Tactical Decisionmaking

* `TacticalActionStrategy` considers actions to take during a turn, delegating to
    * `RangedAttackStrategy`
    * `MeleeAttackStrategy`
    * ... and others as appropriate.