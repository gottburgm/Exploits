This test is for JBAS-3198.  The bug that is being tested for is when using two scoped ejb deployments and
the pooled invoker, the OptimizedObjectInputStream was caching the class definitions when loaded in a static
cache without regard to which classloader the class was originally loaded from.  Therefore, if ejbA made a call
and loaded class Foo (with serialVersionUID = 1L) and then ejbB tried to load class Foo (which would have
a serialVerionUID = 2L), the first Foo class loaded (i.e. serialVersionUID = 1L) would be retreived from the
cache and used instead of loading the correct class.

For the tests themselves, have created two ejb deployments, A and B, which are both scoped deployments.
Both return SimpleResponseDTO.  The test clients (ScopedUnitTestCase and ScopedBUnitTestCase) use
javassist to create a new copy of the SimpleResponseDTO class (one with serialiVersionUID = 1L and the other
with serialVersionUID = 2L) that gets included its ejb deployment and on local classpath (for the client).

To properly test, have to run both ScopedUnitTestCase and then ScopedBUnitTestCase (or vise versa) against
same running instance of JBoss server (i.e. running one, then re-booting JBossAS and running the other does
not test for this bug fix).