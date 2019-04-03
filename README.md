# Overview
RulesEngine is a lightweight open source framework written in Java to develop rules 
based models using a backtracking, generative search mechanism that validates a given 
set of inputs against their applicable rules to find a solution if one exists.

The approach used is based on the concept of backtracking described here: 
https://en.wikipedia.org/wiki/Backtracking

Real-world applications of such rules based models include complex product configuration 
problems encountered in computer, telecommunications and aerospace industries.
 
# Model Definition
To use the RulesEngine framework, the problem domain must first be represented in the
prescribed object model format. This format consists of four classes of objects, which can
be defined as follows:
* Parent Objects: Independent, individual components that are part of the problem domain. The solution for a specific problem consists of a collection of instances of parent objects.
* Child Needers: Dependent constructs associated with parent objects that collectively represent what the parent needs in order to be part of the solution. 
* Child Providers: Dependent constructs associated with parent objects that can satisfy child needers.
* Rules: Represent domain specific restrictions and preferences that are applied when matching up needers with providers. 

Each of the above classes of objects is represented using a separate model class hierarchy, i.e.,
each hierarchy has a separate root. All the classes in each hierarchy can specify properties 
with values that are inherited by sub-classes, but can be overridden at a lower-level.
Properties are either single-valued or lists, and support string and numeric values.

Each class can also define specific model data objects that inherit directly from the class.
These model data object have overridden values for the defined properties ass desired. All model 
data objects in the parent object hierarchy can be instantiated for inclusion in the solution. 

When instantiated, parent objects also instantiate their dependent child needers and 
providers as part of their initialization. Rules are never instantiated in a solution, 
but provide an extensible Java interface layer used to codify domain specific restrictions and 
preferences that are applied when satisfying child needers with child providers.    

Model hierarchy classes and corresponding data are defined using the JSON format, with a 
separate JSON file for each class. The filename must match the class name. Collectively, 
these JSON files for a model form a knowledge base. When loaded into memory, each 
knowledge base also includes a copy of the base and common core classes provided in the 
RulesEngine code.  

# Rule Validation
Each validation operates on a set of input selections in an attempt to find a solution. 
Input selections are parent objects that are desired in the solution. Each input selection
is a parent object with a specified quantity. 

The RulesEngine framework applies the model rules to instances of the parent objects representing 
the inputs to determine what else must be included to produce a valid solution. Each unit quantity 
of a selected input is validated separately, i.e., allowed to succeed or fail separately. 

In order for a parent object to succeed, all of its child needers must be fully satisfied. 
To satisfy a child needer, a valid child provider that provides a consumable resource of the 
correct type and quantity is needed. The validation logic will always attempt to use providers 
that already exist in the solution as it is being constructed, before attempting to create a 
new parent object that has a compatible child provider. Any new provider parent created to satisfy 
a needer, is itself validated prior to its use by the original needer, and this nesting/stacking
of validation steps continues till it is no longer necessary. At any step, if there is no way to 
satisfy a needer, the validation logic backtracks (all related solution state changes are undone in
reverse order) to the next available alternative for the most recent previous validation step that has one.
 
Rules are used to determine the validity and preference of providers at each validation step. Each
applicable rule is evaluated by calling the corresponding Java interface methods and using the model
rule data specified to filter and sort the valid candidates per the rule's logic. 

If only a fully valid solution is acceptable for the problem, i.e., no separate success or failure 
across inputs, then a single validation stack must be used. The RulesEngine code can be extended to 
support this. In some cases, creative model definition that aggregates the inputs may be enough. For 
real-world problems, this is a less common scenario. It results in combinatorial expansion of the 
search space, and consequently longer run time performance. Additionally, for large models caching 
of model hierarchy data, and property values at the lowest-level can be used to enhance performance 
in exchange for a larger memory footprint.

# Example
The problem domain consists of three components: A, B and C. The input for any problem involves specifying
the quantity of A desired. The rules state that for each A selected as input, a B or a C must be 
in the solution, with B being preferred over C, when possible. B's are restricted to a maximum of
10 in any solution.

To create a model for this problem, we would define the following:
* Parent objects for A, B and C
* Child needer for A (An)
* Child providers for B and C (Bp, Cp)
* Rule for enforcing a maximum when creating new instances of B
* Rule for sorting provider candidate B ahead of C

If the input was quantity 15 of A, validation would result in a solution with Ax15, Bx10, Cx5    

Now, let's change the rules to say that each A needs a B and each B needs a C. To do this, we would add
a child needer on B (Bn) and change the compatibility of the needers and providers such that only An matches 
Bp and only Bn matches Cp. 

If the input was quantity 15 of A, validation would result in a partial solution with Ax10, Bx10, Cx10 and 
note that the remaining Ax5 could not be satisfied. 
