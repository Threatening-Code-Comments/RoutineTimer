package de.threateningcodecomments.routinetimer

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textview.MaterialTextView
import com.google.android.material.transition.platform.MaterialContainerTransform
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import de.threateningcodecomments.accessibility.*

class StartFragment : Fragment(), View.OnClickListener, UIContainer {
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var mAuth: FirebaseAuth
    private lateinit var activity: Activity
    private var account: FirebaseUser? = null

    private lateinit var profilepicView: ShapeableImageView
    private lateinit var usernameView: MaterialTextView
    private lateinit var nameCardView: MaterialCardView
    private var isLoggedIn = false

    private lateinit var routineButton: MaterialButton
    private lateinit var settingsButton: MaterialButton
    private lateinit var testButton: MaterialButton


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        sharedElementEnterTransition = RC.Resources.sharedElementTransition

        super.onViewCreated(view, savedInstanceState)

        activity = getActivity() as Activity

        initGSignIn()

        initBufferViews()

        account = FirebaseAuth.getInstance().currentUser
        if (account == null) {
            isLoggedIn = false
            updateUI()
            toggleSignIn()
        } else {
            isLoggedIn = true
            RC.Db.loadDatabaseRes()
        }

        initOnClicks()

        RC.Db.loadDatabaseRes()

        RC.initIconPack(activity)
        RC.Resources.errorDrawable =
                ResourcesCompat.getDrawable(resources, R.drawable.ic_defaultdrawable, activity.theme)!!

        doTestButton()
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.cv_StartFragment_name -> toggleSignIn()
            R.id.btn_StartFragment_routines -> {
                val selectRoutineFragment = SelectRoutineFragment()
                selectRoutineFragment.sharedElementEnterTransition = MaterialContainerTransform()

                //val extras = FragmentNavigatorExtras(v to "container")
                val directions = StartFragmentDirections.actionStartFragmentToSelectEditRoutineFragment()

                findNavController().navigate(directions)//, extras)
            }
            R.id.btn_mainActivity_test -> {
                doTestButton()
            }
            R.id.btn_StartFragment_settings -> {
                val directions = StartFragmentDirections.actionStartFragmentToSettingsFragment()
                findNavController().navigate(directions)
            }
            else -> Toast.makeText(context, "Unknown Error, please see developer or priest", Toast.LENGTH_LONG).show()
        }
    }

    private fun doTestButton() {
        /*val debugActivated = MainActivity.sharedPreferences.getBoolean(getString(R.string.pref_dev_debug_key),
                        false)
                Toast.makeText(context, "debug mode: $debugActivated", Toast.LENGTH_SHORT).show()*/

        val routine = RC.Db.generateRandomRoutine()
        val tile = routine.tiles[0]

        routine.mode = Routine.MODE_CONTINUOUS
        RC.routines.add(routine)
        RC.Db.saveRoutine(routine)

        val directions =
        //StartFragmentDirections.actionStartFragmentToRunContinuousRoutine(routine.uid)
                //StartFragmentDirections.actionStartFragmentToEditContinuousRoutineFragment(routine.uid)
                StartFragmentDirections.actionStartFragmentToTileSettingsFragment(routine.uid, tile.uid)

        findNavController().navigate(directions)
    }

    //region init
    override fun onStart() {
        super.onStart()
        updateUI()
    }

    private fun initBufferViews() {
        val v = requireView()

        routineButton = v.findViewById(R.id.btn_StartFragment_routines)
        settingsButton = v.findViewById(R.id.btn_StartFragment_settings)
        testButton = v.findViewById(R.id.btn_mainActivity_test)

        usernameView = v.findViewById(R.id.tv_StartFragment_username)
        profilepicView = v.findViewById(R.id.iv_StartFragment_profilepic)
        nameCardView = v.findViewById(R.id.cv_StartFragment_name)
    }

    private fun initOnClicks() {
        routineButton.setOnClickListener(this)
        settingsButton.setOnClickListener(this)
        testButton.setOnClickListener(this)

        nameCardView.setOnClickListener(this)
    }

    private fun initGSignIn() {
        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(activity, gso)
        mAuth = FirebaseAuth.getInstance()
    }

    private fun updateButtonClickable() {
        if (!isLoggedIn) {
            testButton.isEnabled = false
            routineButton.isEnabled = false
        } else {
            testButton.isEnabled = true
            routineButton.isEnabled = true
        }
    }

    //endregion

    //region Greeting
    override fun updateUI() {
        account = FirebaseAuth.getInstance().currentUser
        if (account == null) {
            handleGreetNoAccount()
        } else {
            handleGreetAccount()
        }
        updateButtonClickable()
    }

    private fun setGreetTextColor(bgColor: Int) {
        val textColor = RC.Conversions.Colors.calculateContrast(bgColor)
        usernameView.setTextColor(textColor)
        if (!isLoggedIn) {
            profilepicView.setColorFilter(textColor)
        } else {
            profilepicView.clearColorFilter()
        }
    }

    private fun handleGreetAccount() {
        val message: String = "Welcome " + account!!.displayName + "!"
        val pathToPhoto = account!!.photoUrl
        Glide.with(this).load(pathToPhoto).into(profilepicView)
        Glide.with(this).asBitmap().load(pathToPhoto).into(object : CustomTarget<Bitmap?>() {
            override fun onLoadCleared(placeholder: Drawable?) {}
            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap?>?) {
                val pixelColor = resource.getPixel(0, 0)
                setGreetTextColor(pixelColor)
                nameCardView.setCardBackgroundColor(pixelColor)
            }
        })
        usernameView.text = message
    }

    private fun handleGreetNoAccount() {
        val errorDrawable = ContextCompat.getDrawable(activity, R.drawable.ic_defaultdrawable)
        profilepicView.setImageDrawable(errorDrawable)
        usernameView.text = getString(R.string.str_tv_MainActivity_login)
        val bgColor: Int = if (RC.isNightMode) {
            Tile.DEFAULT_COLOR_DARK
        } else {
            Tile.DEFAULT_COLOR
        }
        nameCardView.setCardBackgroundColor(bgColor)
        setGreetTextColor(bgColor)
    }

    //endregion

    //region handle sign in

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)
                MyLog.f("firebaseAuthWithGoogle:" + account!!.id)
                firebaseAuthWithGoogle(account.idToken)
            } catch (e: ApiException) {
                isLoggedIn = false

                // Google Sign In failed, update UI appropriately
                MyLog.fe("Google sign in failed", e)
                // ...
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String?) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(activity) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        MyLog.f("signInWithCredential:success")
                        val user = mAuth.currentUser
                        MyLog.f("User UID is: " + user!!.uid)
                        val path = "/users/" + user.uid
                        val key = "displayname"
                        val value: Any? = user.displayName
                        RC.Db.saveToDb(path, key, value)
                        RC.Db.loadDatabaseRes()
                        isLoggedIn = true
                    } else {
                        // If sign in fails, display a message to the user.
                        isLoggedIn = false
                        MyLog.fe("signInWithCredential:failure", task.exception)
                    }
                    updateUI()
                }
    }

    private fun toggleSignIn() {
        updateUI()
        if (account == null) {
            signIn()
        } else {
            signOut()
        }
        updateUI()
        updateButtonClickable()
        RC.Db.loadDatabaseRes()
    }

    private fun signIn() {
        val signInIntent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    private fun signOut() {
        FirebaseAuth.getInstance().signOut()
        mGoogleSignInClient.signOut()
                .addOnCompleteListener(activity) { MyLog.f("Logged out of google account!") }
        Toast.makeText(context, "Signed out!", Toast.LENGTH_SHORT).show()
        isLoggedIn = false
    }

    //endregion

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_start, container, false)
    }

    override fun updateCurrentTile() {}

    companion object {
        private const val RC_SIGN_IN = 123
    }
}