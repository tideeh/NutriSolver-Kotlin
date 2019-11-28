package br.com.nutrisolver.activitys

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.PorterDuff
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.viewpager.widget.ViewPager
import br.com.nutrisolver.R
import br.com.nutrisolver.objects.Fazenda
import br.com.nutrisolver.tools.DataBaseUtil
import br.com.nutrisolver.tools.TabsAdapter
import br.com.nutrisolver.tools.UserUtil
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.android.material.tabs.TabLayout
import com.google.firebase.firestore.QuerySnapshot
import java.util.*
import kotlin.collections.ArrayList

class Principal : AppCompatActivity() {
    internal lateinit var lotesFragment: LotesFragment
    internal lateinit var dietasFragment: DietasFragment
    internal lateinit var testesFragment: TestesFragment
    internal lateinit var dataFromActivityToLotesFragment: DataFromActivityToFragment
    internal lateinit var dataFromActivityToDietasFragment: DataFromActivityToFragment
    internal lateinit var dataFromActivityToTestesFragment: DataFromActivityToFragment
    private var first_select_ignored: Boolean = false

    private val tabIcons =
        intArrayOf(R.drawable.tab_lotes2, R.drawable.tab_dietas2, R.drawable.tab_testes2)

    private val tabTexts = arrayOf("LOTES", "DIETAS", "TESTES")

    private val sharedpreferences: SharedPreferences by lazy {
        getSharedPreferences("MyPref", Context.MODE_PRIVATE)
    }
    private var fazenda_corrente_id: String = "-1"
    private var fazenda_corrente_nome: String = "-1"
    private lateinit var tabsAdapter: TabsAdapter
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager

    private lateinit var mDrawerLayout: DrawerLayout
    private lateinit var mDrawerToggle: ActionBarDrawerToggle
    private var fazendas_nomes: ArrayList<String> = ArrayList()
    private var fazendas_ids: ArrayList<String> = ArrayList()
    private lateinit var spinner: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nova_main)

        first_select_ignored = false

        fazenda_corrente_id = sharedpreferences.getString("fazenda_corrente_id", "-1") ?: "-1"
        fazenda_corrente_nome = sharedpreferences.getString("fazenda_corrente_nome", "-1") ?: "-1"
        spinner = findViewById(R.id.spn_fazendas)

        configura_toolbar_com_nav_drawer()

        if (savedInstanceState != null) {
            // se ja foram criados, utiliza eles
            lotesFragment = supportFragmentManager.getFragment(
                savedInstanceState,
                "lotesFragment"
            ) as LotesFragment? ?: LotesFragment()
            dietasFragment = supportFragmentManager.getFragment(
                savedInstanceState,
                "dietasFragment"
            ) as DietasFragment? ?: DietasFragment()
            testesFragment = supportFragmentManager.getFragment(
                savedInstanceState,
                "testesFragment"
            ) as TestesFragment? ?: TestesFragment()
        } else {
            lotesFragment = LotesFragment()
            dietasFragment = DietasFragment()
            testesFragment = TestesFragment()
        }

        dataFromActivityToLotesFragment = lotesFragment
        dataFromActivityToDietasFragment = dietasFragment
        dataFromActivityToTestesFragment = testesFragment

        inicia_tab_fragments()

        configura_spinner_fazendas()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        // salva os fragmentos
        supportFragmentManager.putFragment(outState, "lotesFragment", lotesFragment)
        supportFragmentManager.putFragment(outState, "dietasFragment", dietasFragment)
        supportFragmentManager.putFragment(outState, "testesFragment", testesFragment)
    }

    override fun onStart() {
        super.onStart()

        // fecha o navigation drawer
        mDrawerLayout = findViewById(R.id.drawer_layout)
        mDrawerLayout.closeDrawers()
    }

    private fun inicia_tab_fragments() {
        tabsAdapter = TabsAdapter(supportFragmentManager, 3)
        viewPager = findViewById(R.id.view_pager)
        tabLayout = findViewById(R.id.tabLayout)

        tabsAdapter.add(lotesFragment, "Lotes")
        tabsAdapter.add(dietasFragment, "Dietas")
        tabsAdapter.add(testesFragment, "Testes")
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

    private fun configura_toolbar_com_nav_drawer() {
        val my_toolbar = findViewById<Toolbar>(R.id.my_toolbar_main)
        setSupportActionBar(my_toolbar)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        mDrawerLayout = findViewById(R.id.drawer_layout)
        mDrawerToggle = object :
            ActionBarDrawerToggle(
                this,
                mDrawerLayout,
                my_toolbar,
                R.string.drawer_open,
                R.string.drawer_close
            ) {
        }
        mDrawerLayout.addDrawerListener(mDrawerToggle)
        mDrawerLayout.post { mDrawerToggle.syncState() }

        supportActionBar?.title = "Fazenda: " + fazenda_corrente_nome
    }

    private fun configura_spinner_fazendas() {
        fazendas_nomes = ArrayList()
        fazendas_ids = ArrayList()

        DataBaseUtil.getDocumentsWhereEqualTo(
            "fazendas",
            "dono_uid",
            UserUtil.getCurrentUser()?.uid ?: "-1"
        )
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val documents = task.result
                    if (documents != null) {
                        for (document in documents) {
                            fazendas_nomes.add(document.toObject(Fazenda::class.java).nome)
                            fazendas_ids.add(document.toObject(Fazenda::class.java).id)
                        }

                        val opcoes = fazendas_nomes.toTypedArray()
                        val spn_adapter =
                            ArrayAdapter(this@Principal, R.layout.spinner_layout, opcoes)
                        spinner.adapter = spn_adapter

                        spinner.setSelection(fazendas_ids.indexOf(fazenda_corrente_id))

                        spinner.onItemSelectedListener =
                            object : AdapterView.OnItemSelectedListener {
                                override fun onItemSelected(
                                    parent: AdapterView<*>,
                                    view: View,
                                    position: Int,
                                    id: Long
                                ) {
                                    if (first_select_ignored) {

                                        val editor = sharedpreferences.edit()
                                        editor.putString(
                                            "fazenda_corrente_id",
                                            fazendas_ids[position]
                                        )
                                        editor.putString(
                                            "fazenda_corrente_nome",
                                            fazendas_nomes[position]
                                        )
                                        editor.apply()
                                        // fecha o navigation drawer
                                        mDrawerLayout = findViewById(R.id.drawer_layout)
                                        mDrawerLayout.closeDrawers()

                                        fazenda_corrente_nome = fazendas_nomes[position]
                                        fazenda_corrente_id = fazendas_ids[position]

                                        //envia_sinal_pros_fragments();
                                        dataFromActivityToLotesFragment.sendData(
                                            "atualiza_lotes",
                                            null
                                        )
                                        dataFromActivityToDietasFragment.sendData(
                                            "atualiza_dietas",
                                            null
                                        )

                                        supportActionBar?.setTitle("Fazenda: " + fazenda_corrente_nome)
                                    }
                                    first_select_ignored = true
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
                return true
            }

            R.id.mi_refresh -> return true

            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun logout() {
        UserUtil.logOut()

        val editor = sharedpreferences.edit()
        editor.remove("fazenda_corrente_id")
        editor.remove("fazenda_corrente_nome")
        editor.apply()

        startActivity(Intent(this, Login::class.java))
        finish()
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

    fun sidebar_testar_amostra(v: View) {
        startActivity(Intent(this, ExecutarTeste1::class.java))
    }

    fun sendData(fragment: String, data: String, `object`: Any) {
        when (fragment) {
            "DietasFragment" -> {
                dataFromActivityToDietasFragment.sendData(data, `object`)
            }

            "LotesFragment" -> {
                dataFromActivityToLotesFragment.sendData(data, `object`)
            }

            "TestesFragment" -> {
                dataFromActivityToTestesFragment.sendData(data, `object`)
            }

            else -> {
            }
        }
    }
}
