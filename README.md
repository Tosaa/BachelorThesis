# AndroidTemplates
This Repo tries to provide a quick start into a new Android App, without initial setup of wide used dependencies.
The Project uses:
- Dagger-Hilt (Dependency injection tool)
- Androidx (Reworked support libary of Android Jetpack)
- Data Binding (Bind viewmodels to layouts to ease updating views)
- Material Design (Simple good looking components)
- KTX libaries (Improve code quality by helpfull extension functions)
- Android Navigation component (Navigation through all screens)

# BASICS:
- **MVVM** (Model View ViewModel)
- **One Module only** (This approach might not be the best when it comes to multiple modules in the project)
- **Kotlin** (Code generation for Java is not within the Dependencies)

# Architecture
This Repo will provide 3 Screens:
- Main Screen (Create your content here)
- About Screen (Tell the user something about you)
- Settings Screen (Customize the users experience)

The Screens are attached to the same Activity and are controlled by one *androidx.navigation.fragment.NavHostFragment*.  
This NavHostFragment belongs to the MainActivity. (This Template provides only one Activity)

# Tipps:
## Hilt:
- use the **@AndroidEntryPoint** for new Activities and Fragments
- use the **@HiltViewModel** for ViewModels
- create a new Module for each scope that is needed ("ApplicationModule.kt" will hold all Instances which are mandatory for all Fragments, usecases etc.)
- always make sure the Activity, the Fragment belongs to is Annotated correctly
- always make sure the Application, the Activity belongs to is Annotaed correctly

## Use Repositories and UseCases:
- for seperation of concerns, it's highly recommended to use different Repositories to manage Data
- for seperation of concerns, it's highly recommended to use different UseCases, which use different Repositories

## Use Shared Preferences
- using shared preferences makes it easy to create a settings page where the user can personalize his app
- using shared preferences makes it easy to read preferences at every occurrance in the code

