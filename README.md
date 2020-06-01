# Warhammer 40K RPG Combat Simulator

[![CircleCI](https://circleci.com/gh/HSAR/CombatSimulator.svg?style=shield)](https://circleci.com/gh/circleci/circleci-docs)


Simulates combat using rules from Warhammer 40K tabletop roleplaying games to aid in encounter design.

## Usage

Running the command-line interface with any valid command will output usage information.

### Commands

##### `simulate-combat`

Run a simulation set up using the following arguments:

`--friendlies` (required) Path to a file on the local filesystem that contains a JSON representation of the protagonist combatant force.
Examples of the format for this file can be seen in the test resources for this project.

`--enemies` (required) Path to a file on the local filesystem that contains a JSON representation of the opposing combatant force.
Examples of the format for this file can be seen in the test resources for this project.

`--simulations` (optional, default 10) The number of times the simulation should be run. 
As the system simulates a large number of random events during combat, a number of simulations should be run and collated to ensure that any outliers do not skew overall conclusions.
Larger numbers of simulations increase the accuracy of resulting averages but take a longer time to complete.