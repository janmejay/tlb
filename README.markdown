# Rationale:
A typical problem that teams doing CI (continuous integration) try and solve is to get the build time to an acceptable amount so that frequent checkins are possible. However, with a steady increase in the number of tests, the time taken to run these tests on every checkin keeps increasing. Solving this problem in the build is almost always non trivial. This is where parallelizing builds comes handy. Throwing hardware at this problem is one of the potential solutions to get build time under acceptable limits.

**TestLoadBalancer (TLB)** aims at splitting your entire test suite into mutually exclusive units such that each of the unit can be executed in parallel. Assuming that tests are written independent of each other, which is a best practice in writing tests, the tests can be ordered and arranged in anyway and *TLB* leverages this fact in order to split the test suite and reorder the tests.

# Concepts:
*TLB* has 2 main concepts.

## Splitting tests: 
Given a test suite, *TLB* splits it into a given number of mutually exclusive units based on one or a chain of criteria. For example, if a test suite has a total of 40 tests and it needs to be split into 4 units, a potential split could be: 10, 8, 8 and 14 tests or 10 in each of the 4 units. The splits need not be equal and it is completely governed by the criteria. 

*TLB*, comes with 3 criteria. 

### Count Based Criterion:
This criterion splits the test suite into a given number of units such that each unit has an equal or optimally equal number of tests.

#### For example: 
  * 20 tests and 1 unit => 1 unit with 20 tests.
  * 18 tests and 3 units => 3 units with 6 tests.
  * 39 tests and 6 units => 3 units with 6 tests and 3 units with 7 tests each. 

### Time Based Criterion:
This criterion splits the test suite into a given number of units such that each unit when executed takes the same or optimally same amount of time. For the very first run with this criterion, *TLB* uses count based approach and writes the test times along with the test time and publishes it to a central repository. On subsequent runs, it uses this to figure how to split the test suite. 

#### For example:
  * 20 tests and 1 unit => 1 unit with 20 tests.
  * 10 tests and 2 units with each taking 2 minutes => 2 units with 5 tests each taking 10 minutes each.
  * 18 tests and 3 units, with 1 taking 13 minutes and rest taking 1 minute => 1 unit with the 13 minute test, 1 unit with 9 tests taking 9 minutes, 1 unit with 8 tests taking 8 minutes. 

### Composite Criterion:
This criterion delegates to a criteria chain, trying criterion in order, until it exhausts the last one. This is generally the way we use 'time based' setup. We make a chain of 'time based splitter' followed by 'count based splitter' so if time based splitting fails, which may happen because a server(the whiteboard tlb runs against) holding artifact with test times is down or the artifact file doesn't exist, it defaults to 'count balancing' which doesn't require the 'test suite time' artifact. If all criterion in the chain fail, the build fails. This is preferred way of using 'time based criterion'.

The test split criteria is passed in using the environment variable *TLB_CRITERIA*. This is a comma separated list of fully qualified names of criteria classes. If nothing is mentioned, *TLB* by default uses a criteria that doesn't do any balancing at all. 

## Ordering tests:
  *TLB*, along with load balancing can also set the order in which the tests get executed. This can be a useful feature. For instance one can execute the tests which failed in the previous run first, before running other tests. Leveraging the fact that tests are not dependent, ordering can be used to do nifty things. However, this should not be misused so as to run tests in a given order.
 *TLB* has 1 built-in Orderer – FailedTestFirst orderer. This runs the test that are known to have failed in the previous run first, before running the remaining tests. *TLB* along with the test times also writes the test status, and uploads that to the central repository. Using this information, it runs the failed tests first in the subsequent runs.
  The ordering criterion is passed in using the environment variable *TLB_ORDERER*. This is a comma separated list of fully qualified names of orderer classes. If nothing is mentioned, *TLB* does not order the tests.

# Supported Frameworks:
 *TLB* assumes that a test framework provides an option to specify a list of file resources that gets executed. The initial list is passed to the criteria chain. Splitter criterion prunes the file resource list and passes on to the test running framework to run.

After this the list of the file resources is passed through the orderer, where it gets reordered. The contract is that the orderer does not change the number of file resources. 

The final list of file resources is what is fed into the test framework. 

As of now, *TLB* supports running JUnit tests using Ant and Buildr(which uses underlying ant infrastructure). We are in the process of adding support for load balancing [Twist™](http://www.thoughtworks-studios.com/agile-test-automation "ThoughtWorks Studios - Twist") again running on ant/buildr. Supporting other build tools testing frameworks is a matter of implementing the end user interface which delegate to the Splitter and Orderer.

# Contributors:
## Core Team:
  * Pavan K Sudarshan [http://github.com/itspanzi](http://github.com/itspanzi "Github Page")
  * Janmejay Singh [http://codehunk.wordpress.com](http://codehunk.wordpress.com "Blog")
## Other Contributors:
  * Chris Turner [http://github.com/BestFriendChris](http://github.com/BestFriendChris "Github Page")

# History:
  *TLB* started in Jan 2010 as an attempt to enhance another similar project called TestLoadBalancer(hosted on code.google.com and github.com). However due to some other issues inheriting the codebase for reuse/enhancement was not possible, besides the direction planned for the new codebase was not aligned with the structure of TestLoadBalancer codebase, so we decided to build *TLB* from scratch. The old TestLoadBalancer originally implemented the idea of load balancing tests based on count, and inspired the creation of *TLB*. The project is not developed or maintained anymore and is not hosted publicly. 

## People behind the TestLoadBalancer(the old project)
  * Li Yanhui [http://whimet.blogspot.com](http://whimet.blogspot.com "Blog")
  * Hu Kai [http://iamhukai.com](http://iamhukai.com "Blog")
  * Derek Yang [http://dyang.github.com/](http://dyang.github.com/ "Github Page")
