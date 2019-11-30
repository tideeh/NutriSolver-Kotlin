package br.com.nutrisolver.fragments


import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import br.com.nutrisolver.R
import br.com.nutrisolver.activitys.ExecutarTeste1Activity
import br.com.nutrisolver.activitys.PrincipalActivity

/**
 * A simple [Fragment] subclass.
 */
class TestesFragment : Fragment(),
    PrincipalActivity.DataFromActivityToFragment {
    lateinit var my_view: View

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_testes, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        this.my_view = view

        view.findViewById<Button>(R.id.btn_executar_teste).setOnClickListener {
            val ite = Intent(activity, ExecutarTeste1Activity::class.java)
            startActivity(ite)
        }
    }

    override fun sendData(data: String, `object`: Any?) {
    }
}
