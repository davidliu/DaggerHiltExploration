# DaggerHiltExploration

An exploration into the Hilt library introduced in [Dagger 2.28](https://dagger.dev/api/2.28/).

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
EntryPointAccessors.fromActivity(this, RepositoryLocator::class.java).getRepository()
````

I avoid using provision methods in my projects, but if you use them currently, this would be how you migrate.

