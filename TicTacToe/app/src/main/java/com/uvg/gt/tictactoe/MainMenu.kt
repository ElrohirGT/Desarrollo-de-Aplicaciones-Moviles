package com.uvg.gt.tictactoe

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.navigation.fragment.findNavController

/**
 * A simple [Fragment] subclass.
 * Use the [MainMenu.newInstance] factory method to
 * create an instance of this fragment.
 */
class MainMenu : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_main_menu, container, false)
        val btn3x3 = view.findViewById<Button>(R.id.btn3x3);
        val btn4x4 = view.findViewById<Button>(R.id.btn4x4);
        val btn5x5 = view.findViewById<Button>(R.id.btn5x5);

        btn3x3.setOnClickListener {
            navigateToGame(3)
        }
        btn4x4.setOnClickListener {
            navigateToGame(4)
        }
        btn5x5.setOnClickListener {
            navigateToGame(5)
        }

        return view
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @return A new instance of fragment MainMenu.
         */
        @JvmStatic
        fun newInstance() =
            MainMenu().apply {}
    }

    private fun navigateToGame(gridSize: Int) {
        val navController = findNavController();
        navController.navigate(R.id.action_mainMenu_to_game, Bundle().apply {
            putInt(GRID_SIZE, gridSize)
        });
    }
}