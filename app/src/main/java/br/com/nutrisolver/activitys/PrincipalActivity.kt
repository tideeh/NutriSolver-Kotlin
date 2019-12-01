package br.com.nutrisolver.activitys

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.PorterDuff
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.viewpager.widget.ViewPager
import br.com.nutrisolver.R
import br.com.nutrisolver.models.Fazenda
import br.com.nutrisolver.adapters.TabsAdapter
import br.com.nutrisolver.fragments.DietasFragment
import br.com.nutrisolver.fragments.LotesFragment
import br.com.nutrisolver.fragments.TestesFragment
import br.com.nutrisolver.utils.*
import com.google.android.material.tabs.TabLayout

class PrincipalActivity : AppCompatActivity() {
    private lateinit var lotesFragment: LotesFragment
    private lateinit var dietasFragment: DietasFragment
    private lateinit var testesFragment: TestesFragment

    private var firstSelectIgnored: Boolean = false

    private val tabIcons =
        intArrayOf(R.drawable.tab_lotes2, R.drawable.tab_dietas2, R.drawable.tab_testes2)

    private lateinit var sharedPreferences: SharedPreferences

    private var fazendaCorrenteId: String = DEFAULT_STRING_VALUE
    private var fazendaCorrenteNome: String = DEFAULT_STRING_VALUE

    private lateinit var tabsAdapter: TabsAdapter
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager

    private lateinit var mDrawerLayout: DrawerLayout
    private lateinit var mDrawerToggle: ActionBarDrawerToggle

    private var listFazendasNomes: ArrayList<String> = ArrayList()
    private var listFazendasIds: ArrayList<String> = ArrayList()

    private lateinit var spinner: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_principal)

        sharedPreferences = getSharedPreferences(SP_NOME, Context.MODE_PRIVATE)

        firstSelectIgnored = false

        fazendaCorrenteId = sharedPreferences.getString(SP_KEY_FAZENDA_CORRENTE_ID, DEFAULT_STRING_VALUE) ?: DEFAULT_STRING_VALUE
        fazendaCorrenteNome = sharedPreferences.getString(SP_KEY_FAZENDA_CORRENTE_NOME, DEFAULT_STRING_VALUE) ?: DEFAULT_STRING_VALUE
        spinner = findViewById(R.id.spn_fazendas)

        configuraToolbarComNavDrawer()

        if (savedInstanceState != null) {
            // se ja foram criados, utiliza eles
            lotesFragment = supportFragmentManager.getFragment(
                savedInstanceState,
                BUNDLE_KEY_LOTES_FRAGMENT
            ) as LotesFragment? ?: LotesFragment()
            dietasFragment = supportFragmentManager.getFragment(
                savedInstanceState,
                BUNDLE_KEY_DIETAS_FRAGMENT
            ) as DietasFragment? ?: DietasFragment()
            testesFragment = supportFragmentManager.getFragment(
                savedInstanceState,
                BUNDLE_KEY_TESTES_FRAGMENT
            ) as TestesFragment? ?: TestesFragment()
        } else {
            lotesFragment = LotesFragment()
            dietasFragment = DietasFragment()
            testesFragment = TestesFragment()
        }

        dataFromActivityToLotesFragment = lotesFragment
        dataFromActivityToDietasFragment = dietasFragment
        dataFromActivityToTestesFragment = testesFragment

        iniciaTabFragments()

        configuraSpinnerFazendas()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        // salva os fragmentos
        supportFragmentManager.putFragment(outState, BUNDLE_KEY_LOTES_FRAGMENT, lotesFragment)
        supportFragmentManager.putFragment(outState, BUNDLE_KEY_DIETAS_FRAGMENT, dietasFragment)
        supportFragmentManager.putFragment(outState, BUNDLE_KEY_TESTES_FRAGMENT, testesFragment)
    }

    override fun onStart() {
        super.onStart()

        if (!UserUtil.isLogged()) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        // fecha o navigation drawer
        mDrawerLayout = findViewById(R.id.drawer_layout)
        mDrawerLayout.closeDrawers()
    }

    private fun iniciaTabFragments() {
        tabsAdapter =
            TabsAdapter(supportFragmentManager, 3)
        viewPager = findViewById(R.id.view_pager)
        tabLayout = findViewById(R.id.tabLayout)

        tabsAdapter.add(lotesFragment, getString(R.string.lotes))
        tabsAdapter.add(dietasFragment, getString(R.string.dietas))
        tabsAdapter.add(testesFragment, getString(R.string.testes))
        viewPager.adapter = tabsAdapter
        viewPager.offscreenPageLimit = 3
        tabLayout.setupWithViewPager(viewPager)

        setupTabIcons()
    }

    private fun setupTabIcons() {

        tabLayout.getTabAt(0)?.setIcon(tabIcons[0])
        tabLayout.getTabAt(1)?.setIcon(tabIcons[1])
        tabLayout.getTabAt(2)?.setIcon(tabIcons[2])

        tabLayout.getTabAt(0)?.icon?.setColorFilter(
            resources.getColor(R.color.tabLayout_selected_icon_color),
            PorterDuff.Mode.SRC_IN
        )
        tabLayout.getTabAt(1)?.icon?.setColorFilter(
            resources.getColor(R.color.tabLayout_unselected_icon_color),
            PorterDuff.Mode.SRC_IN
        )
        tabLayout.getTabAt(2)?.icon?.setColorFilter(
            resources.getColor(R.color.tabLayout_unselected_icon_color),
            PorterDuff.Mode.SRC_IN
        )

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                tab.icon?.setColorFilter(
                    resources.getColor(R.color.tabLayout_selected_icon_color),
                    PorterDuff.Mode.SRC_IN
                )
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
                tab.icon?.setColorFilter(
                    resources.getColor(R.color.tabLayout_unselected_icon_color),
                    PorterDuff.Mode.SRC_IN
                )
            }

            override fun onTabReselected(tab: TabLayout.Tab) {

            }
        })

    }

    private fun configuraToolbarComNavDrawer() {
        val myToolbar = findViewById<Toolbar>(R.id.my_toolbar_main)
        setSupportActionBar(myToolbar)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        mDrawerLayout = findViewById(R.id.drawer_layout)
        mDrawerToggle = object :
            ActionBarDrawerToggle(
                this,
                mDrawerLayout,
                myToolbar,
                R.string.drawer_open,
                R.string.drawer_close
            ) {
        }
        mDrawerLayout.addDrawerListener(mDrawerToggle)
        mDrawerLayout.post { mDrawerToggle.syncState() }

        supportActionBar?.title = getString(R.string.fazenda)+": $fazendaCorrenteNome"
    }

    private fun configuraSpinnerFazendas() {
        listFazendasNomes = ArrayList()
        listFazendasIds = ArrayList()

        Log.i("MY_MEMBER_NAME", Fazenda::donoUid.name)
        DataBaseUtil.getDocumentsWhereEqualTo(
            DB_COLLECTION_FAZENDAS,
            Fazenda::donoUid.name,
            UserUtil.getCurrentUser()?.uid ?: DEFAULT_STRING_VALUE
        )
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val documents = task.result
                    if (documents != null) {
                        for (document in documents) {
                            listFazendasNomes.add(document.toObject(Fazenda::class.java).nome)
                            listFazendasIds.add(document.toObject(Fazenda::class.java).id)
                        }

                        val opcoes = listFazendasNomes.toTypedArray()
                        val spnAdapter =
                            ArrayAdapter(this@PrincipalActivity, R.layout.spinner_layout, opcoes)
                        spinner.adapter = spnAdapter

                        spinner.setSelection(listFazendasIds.indexOf(fazendaCorrenteId))

                        spinner.onItemSelectedListener =
                            object : AdapterView.OnItemSelectedListener {
                                override fun onItemSelected(
                                    parent: AdapterView<*>,
                                    view: View,
                                    position: Int,
                                    id: Long
                                ) {
                                    if (firstSelectIgnored) {

                                        val editor = sharedPreferences.edit()
                                        editor.putString(
                                            SP_KEY_FAZENDA_CORRENTE_ID,
                                            listFazendasIds[position]
                                        )
                                        editor.putString(
                                            SP_KEY_FAZENDA_CORRENTE_NOME,
                                            listFazendasNomes[position]
                                        )
                                        editor.apply()
                                        // fecha o navigation drawer
                                        mDrawerLayout = findViewById(R.id.drawer_layout)
                                        mDrawerLayout.closeDrawers()

                                        fazendaCorrenteNome = listFazendasNomes[position]
                                        fazendaCorrenteId = listFazendasIds[position]

                                        //envia_sinal_pros_fragments();
                                        dataFromActivityToLotesFragment.sendData(
                                            SEND_DATA_COMMAND_ATUALIZA_LOTES,
                                            null
                                        )
                                        dataFromActivityToDietasFragment.sendData(
                                            SEND_DATA_COMMAND_ATUALIZA_DIETAS,
                                            null
                                        )

                                        supportActionBar?.title = getString(R.string.fazenda)+": $fazendaCorrenteNome"
                                    }
                                    firstSelectIgnored = true
                                }

                                override fun onNothingSelected(parent: AdapterView<*>) {
                                }
                            }
                    }
                } else {
                    Log.i(
                        "MY_FIRESTORE",
                        "Erro ao recuperar documentos Fazendas: " + task.exception
                    )
                }
            }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }

            R.id.miDeslogar -> {
                logout()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
                return true
            }

            R.id.mi_refresh -> return true

            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun logout() {
        UserUtil.logOut()

        val editor = sharedPreferences.edit()
        editor.remove(SP_KEY_FAZENDA_CORRENTE_ID)
        editor.remove(SP_KEY_FAZENDA_CORRENTE_NOME)
        editor.apply()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.toolbar_itens, menu)

        val mSearch = menu.findItem(R.id.mi_search)
        val mSearchView = mSearch.actionView as SearchView
        mSearchView.queryHint = "Search"
        mSearchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                //mAdapter.getFilter().filter(newText);
                return true
            }
        })

        return super.onCreateOptionsMenu(menu)
    }

    interface DataFromActivityToFragment {
        fun sendData(data: String, `object`: Any?)
    }

    fun sidebarButtonTestarAmostra(v: View) {
        startActivity(Intent(this, ExecutarTeste1Activity::class.java))
    }

    companion object {
        lateinit var dataFromActivityToLotesFragment: DataFromActivityToFragment
        lateinit var dataFromActivityToDietasFragment: DataFromActivityToFragment
        lateinit var dataFromActivityToTestesFragment: DataFromActivityToFragment

        fun sendData(fragment: String, data: String, `object`: Any) {
            when (fragment) {
                SEND_DATA_FRAGMENT_DIETAS -> {
                    dataFromActivityToDietasFragment.sendData(data, `object`)
                }

                SEND_DATA_FRAGMENT_LOTES -> {
                    dataFromActivityToLotesFragment.sendData(data, `object`)
                }

                SEND_DATA_FRAGMENT_TESTES -> {
                    dataFromActivityToTestesFragment.sendData(data, `object`)
                }

                else -> {
                }
            }
        }
    }

}
