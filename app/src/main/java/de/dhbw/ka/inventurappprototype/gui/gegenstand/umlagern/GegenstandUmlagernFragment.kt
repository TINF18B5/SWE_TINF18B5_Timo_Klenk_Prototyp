package de.dhbw.ka.inventurappprototype.gui.gegenstand.umlagern

import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import de.dhbw.ka.inventurappprototype.R
import de.dhbw.ka.inventurappprototype.aktoren.AktorenKontext
import de.dhbw.ka.inventurappprototype.aktoren.EventListener
import de.dhbw.ka.inventurappprototype.daten.events.gegenstandstyp.GegenstandstypErstelltEvent
import de.dhbw.ka.inventurappprototype.daten.gegenstand.Gegenstand
import de.dhbw.ka.inventurappprototype.daten.kommandos.gegenstand.GegenstandAuslagernKommando
import de.dhbw.ka.inventurappprototype.daten.kommandos.gegenstand.GegenstandEinlagernKommando
import de.dhbw.ka.inventurappprototype.daten.kommandos.gegenstandstyp.GegenstandstypErstellenKommando
import de.dhbw.ka.inventurappprototype.gui.ARG_GEGENSTAND
import de.dhbw.ka.inventurappprototype.gui.ARG_RICHTUNG
import de.dhbw.ka.inventurappprototype.kommando_bearbeitung.KommandoErgebnis
import kotlinx.android.synthetic.main.fragment_gegenstand_umlagern.*


/**
 * A simple [Fragment] subclass.
 * Use the [GegenstandUmlagernFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class GegenstandUmlagernFragment : Fragment() {
    private lateinit var gegenstand: Gegenstand
    private var richtung: UmlagerRichtung = UmlagerRichtung.EINLAGERN
    private lateinit var erstelltListener:EventListener<GegenstandstypErstelltEvent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            gegenstand = it.getParcelable(ARG_GEGENSTAND)!!
            richtung = it.getSerializable(ARG_RICHTUNG) as UmlagerRichtung
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_gegenstand_umlagern, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        button_gegenstand_umlagern_umlagern.text = when (richtung) {
            UmlagerRichtung.AUSLAGERN -> getString(R.string.button_auslagern)
            UmlagerRichtung.EINLAGERN -> getString(R.string.button_einlagern)
            UmlagerRichtung.ERSTMALIGES_EINLAGERN -> getString(R.string.button_einlagern)
        }

        content_gegenstand_umlagern_gegenstandstyp_name.text =
            Editable.Factory.getInstance().newEditable(gegenstand.typ.name)
        content_gegenstand_umlagern_gegenstandstyp_beschreibung.text =
            Editable.Factory.getInstance().newEditable(gegenstand.typ.beschreibung)

        content_gegenstand_umlagern_lagerort_name.text =
            gegenstand.ort.name
        content_gegenstand_umlagern_lagerort_beschreibung.text =
            gegenstand.ort.beschreibung

        button_gegenstand_umlagern_umlagern.setOnClickListener {
            val kommando = when (richtung) {
                UmlagerRichtung.EINLAGERN -> GegenstandEinlagernKommando(
                    AktorenKontext.derzeitigerNutzer.name,
                    content_gegenstand_umlagern_menge.text.toString().toInt(),
                    gegenstand.ort.name,
                    gegenstand.typ.ID
                )
                UmlagerRichtung.AUSLAGERN -> GegenstandAuslagernKommando(
                    AktorenKontext.derzeitigerNutzer.name,
                    content_gegenstand_umlagern_menge.text.toString().toInt(),
                    gegenstand.ort.name,
                    gegenstand.typ.ID
                )
                UmlagerRichtung.ERSTMALIGES_EINLAGERN -> GegenstandstypErstellenKommando(
                    AktorenKontext.derzeitigerNutzer.name,
                    content_gegenstand_umlagern_gegenstandstyp_name.text.toString(),
                    content_gegenstand_umlagern_gegenstandstyp_beschreibung.text.toString()
                )
            }

            when (val ergebnis = AktorenKontext.zentralerKommandoProzessor.bearbeite(kommando)) {
                is KommandoErgebnis.NichtAkzeptiert -> Snackbar.make(view, ergebnis.fehler, BaseTransientBottomBar.LENGTH_SHORT)
                    .show()
            }
        }

        button_gegenstand_umlagern_zuruck.setOnClickListener {
            /*
            findNavController().navigate(
                R.id.action_gegenstandUmlagernFragment_to_gegenstandInfoFragment,
                bundleOf(ARG_GEGENSTAND to gegenstand)
            )
             */
            findNavController().popBackStack()
        }
        erstelltListener = {
            richtung = UmlagerRichtung.EINLAGERN
            gegenstand = AktorenKontext.datenbankConnector.gegenstand(
                typID = it.ID,
                lagerortName = gegenstand.ort.name
            )!!
        }
        AktorenKontext.eventStream.register(erstelltListener)
    }

    override fun onDestroy() {
        super.onDestroy()

            AktorenKontext.eventStream.unregister(erstelltListener)

    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param gegenstand Der Gegenstand.
         * @param richtung Die Richtung
         * @return A new instance of fragment GegenstandUmlagernFragment.
         */
        @JvmStatic
        fun newInstance(
            gegenstand: Gegenstand,
            richtung: UmlagerRichtung = UmlagerRichtung.EINLAGERN
        ) =
            GegenstandUmlagernFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_GEGENSTAND, gegenstand)
                    putSerializable(ARG_RICHTUNG, richtung)
                }
            }
    }
}