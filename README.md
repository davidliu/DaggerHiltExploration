# DaggerHiltExploration

An exploration into the Hilt library introduced in [Dagger 2.28](https://dagger.dev/api/2.28/). Keep in mind, that at the time of this writing (5/29/2020), Hilt is still in alpha, and no docs have been released other than the sparse Javadocs, so my interpretations may be erroneous. Feel free to let me know if I've got anything wrong by filing an issue or messaging me on Reddit (/u/Pzychotix).

## Table of Contents
  * [Hilt Basics](#hilt-basics)
    + [@HiltAndroidApp](#hiltandroidapp)
    + [@AndroidEntryPoint](#androidentrypoint)
    + [@InstallIn](#installin)
    + [Components](#components)
    + [@EntryPoint/EntryPointAccessors](#entrypointentrypointaccessors)
    + [Gradle Multi Modules](#gradle-multi-modules)
  * [Testing](#testing)
    + [@UninstallModule](#uninstallmodule)
    + [@BindValue and others](#bindvalue-and-others)
    + [`@Inject` in tests](#--inject--in-tests)
  * [AndroidX Goodies](#androidx-goodies)
    + [Hilt-Work](#hilt-work)
      - [`@WorkerInject`](#workerinject)
      - [HiltWorkerFactory](#hiltworkerfactory)
    + [Hilt-Lifecycle-Viewmodel](#hilt-lifecycle-viewmodel)
      - [`@ViewModelInject`](#viewmodelinject)
      - [Other Configuration](#other-configuration)

<small><i><a href='http://ecotrust-canada.github.io/markdown-toc/'>Table of contents generated with markdown-toc</a></i></small>


## Hilt Basics

### `@HiltAndroidApp`

The first annotation required to use Hilt. This must be placed on your `Application` class.

````
@HiltAndroidApp
class MainApplication : Application()
````

### `@AndroidEntryPoint`

The annotation placed on your Activity/Fragment/Services/etc.
This is roughly equivalent to the DaggerActivity/DaggerFragment classes, which handled calling the relevant injection
methods for you.

````
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var repository: Repository
    ....
}
````

Notably, this supports Views while the old dagger-android injection did not (at least not out of the box).


### `@InstallIn`

This is the new way to add modules to a component. Rather than declaring the module as a part of the
`@Component` annotation, `@InstallIn` allows you to declare which component to add this module to.

````
@Module
@InstallIn(ApplicationComponent::class)
object MainAppModule {
    @Provides
    fun repository() = Repository()
}
````

### Components

[There are many new base Components defined in hilt](https://dagger.dev/api/2.28/dagger/hilt/android/components/package-summary.html), 
which seem to now be **the** components you will be using. Like before, child components will inherit their parent's
bindings, so anything available at the parent level will also be available in the children. In the above code samples,
the MainAppModule is installed at the Application level, and can be injected into the Activity.

From what I can gather, Hilt automatically uses these components without any further declaration,
so installing a module in `FragmentComponent` would make that module available to all Hilt enabled fragments.

I'm not sure if there's any way to change what component is used, which would mean that providing different implementations of an
interface in different components would be impossible. This sounds like a downgrade from dagger-android, so I'd need
further investigation to make sure this is true.

### `@EntryPoint/EntryPointAccessors`

Roughly equivalent to [provision methods](https://dagger.dev/api/latest/dagger/Component.html#provision-methods). 
Since we can't modify the components now, we can install an `@EntryPoint` interface, which will make the target component
conform to the interface.

````
// This EntryPoint makes the ApplicationComponent implement the RepositoryLocator interface.
@EntryPoint
@InstallIn(ApplicationComponent::class)
interface RepositoryLocator {
    fun getRepository(): Repository
}
````

We can then use the EntryPointAccessors utility class to access the component through the interface.

````
EntryPointAccessors.fromApplication(applicationContext, RepositoryLocator::class.java).getRepository()
````

Note that unlike module bindings, entry point interfaces are not inherited by their subcomponents, so an EntryPoint installed
on an ApplicationComponent can only be accessed using the `fromApplication` accessor.

I avoid using provision methods in my projects, preferring to have these things injected directly, but if you use them currently, this would be how you migrate.

### Gradle Multi Modules

A library module can now install into the app module's Component without any interaction needed at the app module level.

````
// Declared in the feature module
@Module
@InstallIn(ApplicationComponent::class)
object FeatureModule {
    @Provides
    fun feature() = Feature()
}

// main app can be injected with Feature
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var feature: Feature
    ....
}

````

This is a nice quality of life change from dagger-android, where a library module would need to manually be added to the
app's graph. Potentially a little sketch though, if 3rd party libraries can abuse this to latch onto your graph. It'd be an interesting attack vector, at the very least.


## Testing

On the testing side, Hilt has really great support compared to barebones Dagger. It provides some simple ways to replace a dependency, whereas Dagger by itself requires quite a bit of organization to do the same.

Thanks to [@remcomokveld](https://github.com/remcomokveld) for kicking this off by [adding an example Hilt test.](https://github.com/davidliu/DaggerHiltExploration/blob/master/app/src/androidTest/java/com/deviange/daggerhilt/HiltExampleTest.kt)

### [`@UninstallModule`](https://dagger.dev/api/2.28/dagger/hilt/android/testing/UninstallModules.html)

Like the name says, this removes a module that's been add with `@InstallIn`. This is super useful for replacing production dependencies with a test version by installing your own test module. 

````
@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
@UninstallModules(FeatureModule::class)
class HiltExampleTest {

    @Module
    @InstallIn(ApplicationComponent::class)
    object FeatureModule {
        @Provides
        fun feature(): Feature = TestFeature()
    }
}
````

The resulting component is still compile-time checked, so if you uninstall a module 
without replacing the needed dependencies (or install a new one without uninstalling the old),
you'll get the standard Dagger errors to cover for you. Additionally, both the `@UninstallModules` 
and the above `@InstallIn` are local to the test class, so they don't affect what you do in other test classes. 

### [`@BindValue`](https://dagger.dev/api/2.28/dagger/hilt/android/testing/BindValue.html) and others

As an alternative to creating and installing a new module for a test dependency, you can use the `@Bind` family
of annotations to directly add it to the component. This is also important if you want to interact with the test dependency
during the test. Keep in mind that Dagger binds by exact type, and this is no different.

````
@HiltAndroidTest
@UninstallModules(FeatureModule::class)
class HiltExampleTest {

    private val testFeature = TestFeature()
    
    @BindValue
    @JvmField
    val feature: Feature = testFeature
}
````

A neat thing that can be done with these test level binds is that they can easily replace classes 
that are not provided through modules, but through the constructor `@Inject` annotation. 
While module overriding is common in Dagger for test overrides, this is something new that's very convenient.

### `@Inject` in tests

Your tests can be injected as well! They'll grab the dependencies from the component that's configured for that test class.

````
@HiltAndroidTest
class SomeTest {
    @get:Rule
    var hiltRule = HiltAndroidRule(this)
    
    @Inject 
    lateinit val dependency: Dependency

    @Before
    fun beforeEach() {
        hiltRule.inject()
        ...
    }
}
````

## AndroidX Goodies

On the AndroidX side, they've been hard at work at integrating Hilt into some specific components. They're currently available as snapshot builds from the [androidx repo](https://androidx.dev/):

````
implementation "androidx.hilt:hilt-work:1.0.0-SNAPSHOT"
implementation "androidx.hilt:hilt-common:1.0.0-SNAPSHOT"
implementation "androidx.hilt:hilt-lifecycle-viewmodel:1.0.0-SNAPSHOT"
kapt "androidx.hilt:hilt-compiler:1.0.0-SNAPSHOT"
````

Thanks to [@R4md4c](https://github.com/R4md4c) for putting in the legwork on all this!

### Hilt-Work

The Hilt-Work artifact provides automatic generation of the common multi-bind worker factory pattern (if you don't know what this is, then now you won't have to!) It will find all workers annotated with `@WorkerInject` and create a `HiltWorkerFactory` that can be used with your WorkManager to inject your workers.

#### `@WorkerInject`

Simply annotate your Workers with `@WorkerInject` and `@Assisted` for the `Context` and `WorkerParameters` in the constructor. Any additional injected dependencies can just be added into the constructor.

````
class RepositoryWorker @WorkerInject constructor(
    private val repository: Repository,
    @Assisted private val context: Context,
    @Assisted private val workerParameters: WorkerParameters
) : Worker(context, workerParameters)
````

#### HiltWorkerFactory

To finish the Hilt-Work setup, you just need to attach it to your WorkManager like so:

````
@HiltAndroidApp
class MainApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun getWorkManagerConfiguration(): Configuration =
        Configuration.Builder().setWorkerFactory(workerFactory).build()
}
````

### Hilt-Lifecycle-Viewmodel

Very similar to the Hilt-Work artifact, this artifact handles all the multi-map binding for ViewModel creation. 

#### `@ViewModelInject`

Simply annotate your ViewModel with `@ViewModelInject`. The constructor can take in any dependencies available at the `@ActivityRetainedComponent` scope, as well as an optional SavedStateHandle marked with an `@Assisted` annotation.
````
class MainViewModel 
@ViewModelInject 
constructor(
    private val repository: Repository,
    @Assisted private val savedStateHandle: SavedStateHandle
): ViewModel()
````

#### Other Configuration

None! As long as the ViewModel is retrieved from an `@AndroidEntryPoint` annotated Activity/Fragment, it will automatically handle getting the injected ViewModel.
