package com.uvg.gt.tictactoe

import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import kotlin.random.Random

const val GRID_SIZE = "gridSize"
const val PLAYER_1 = "player1"
const val PLAYER_2 = "player2"

enum class CellState {
    EMPTY,
    X,
    O
}

enum class Player {
    O,
    X
}

/**
 * A simple [Fragment] subclass.
 * Use the [Game.newInstance] factory method to
 * create an instance of this fragment.
 */
class Game : Fragment() {
    private var gridSize: Int? = null
    private var player1: String? = null
    private var player2: String? = null

    private var gridView: TableLayout? = null
    private var innerGrid: MutableList<MutableList<CellState>>? = null
    private var playerTurn: Player = Player.X
    private var occupiedCellsCount: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            gridSize = it.getInt(GRID_SIZE)
            player1 = it.getString(PLAYER_1)
            player2 = it.getString(PLAYER_2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val screenWidthPx = DisplayMetrics().widthPixels

        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_game, container, false)
        val tvTitle = view.findViewById<TextView>(R.id.tvPlayerTurn)
        val btnBack = view.findViewById<Button>(R.id.btnBackMainMenu)

        if(Random.nextInt() % 2 == 0) {
            changeTurn(tvTitle) // This makes it so that sometimes the O player will start!
        }

        btnBack.setOnClickListener {
            val nav = findNavController()
            nav.navigate(R.id.action_game_to_mainMenu2)
        }

        gridView = view.findViewById(R.id.tlGrid)
        innerGrid =
            MutableList(gridSize!!) { MutableList(gridSize!!) { CellState.EMPTY } }

        val maxIndex = gridSize!! - 1
        for (i in 0..maxIndex step 1) {
            val row = TableRow(context)
            row.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            for (j in 0..maxIndex step 1) {
                val cell = Button(context)
                cell.text = ""
                cell.width = screenWidthPx / gridSize!!
                cell.setOnClickListener {
                    val cellIsOccuppied = innerGrid!![i][j] != CellState.EMPTY
                    if (cellIsOccuppied) {
                        return@setOnClickListener
                    }

                    innerGrid!![i][j] = mapPlayerTurnToCellState(playerTurn)
                    cell.text = playerTurn.toString()
                    occupiedCellsCount++

                    if (checkIfWon(playerTurn, i, j)) {
                        tvTitle.text = mapPlayerTurnToName(playerTurn) + " GANA!"
                        disableAllButtons()
                    } else if (checkIfDraw()) {
                        tvTitle.text = "Empate!"
                    } else {
                        changeTurn(tvTitle)
                    }
                }
                row.addView(cell)
            }
            gridView!!.addView(row)
        }

        return view
    }

    private fun checkIfWon(turn: Player, row: Int, cell: Int): Boolean {
        val playerCellState = mapPlayerTurnToCellState(turn)

        // Positions where the row and cell are on the edge of a check.
        val upperLeftCornerPositions = listOf(
            Pair(row - 1, cell - 1),
            Pair(row - 2, cell - 2)
        )
        val upperSidePositions = listOf(
            Pair(row - 1, cell),
            Pair(row - 2, cell)
        )
        val upperRightCornerPositions = listOf(
            Pair(row - 1, cell + 1),
            Pair(row - 2, cell + 2)
        )
        val rightSidePositions = listOf(
            Pair(row, cell + 1),
            Pair(row, cell + 2)
        )
        val lowerRightCornerPositions = listOf(
            Pair(row + 1, cell + 1),
            Pair(row + 2, cell + 2)
        )
        val lowerSidePositions = listOf(
            Pair(row + 1, cell),
            Pair(row + 2, cell)
        )
        val lowerLeftCornerPositions = listOf(
            Pair(row + 1, cell - 1),
            Pair(row + 2, cell - 2)
        )
        val leftSidePositions = listOf(
            Pair(row, cell - 1),
            Pair(row, cell - 2)
        )

        // Positions where the row and cell are the middle of the 3
        val leftToRightDiagonal = listOf(
            Pair(row - 1, cell - 1),
            Pair(row + 1, cell + 1)
        )
        val rightToLeftDiagonal = listOf(
            Pair(row - 1, cell + 1),
            Pair(row + 1, cell - 1)
        )
        val topToBottom = listOf(
            Pair(row - 1, cell),
            Pair(row + 1, cell)
        )
        val leftToRight = listOf(
            Pair(row, cell - 1),
            Pair(row, cell + 1)
        )

        val positions = listOf(
            upperLeftCornerPositions,
            upperSidePositions,
            upperRightCornerPositions,
            rightSidePositions,
            lowerRightCornerPositions,
            lowerSidePositions,
            lowerLeftCornerPositions,
            leftSidePositions,
            leftToRightDiagonal,
            rightToLeftDiagonal,
            topToBottom,
            leftToRight
        )
        positions.forEach { pos ->
            val hasWon = checkPositions(playerCellState, pos)
            if (hasWon) {
                return true
            }
        }
        return false
    }

    private fun checkPositions(stateTurn: CellState, positions: List<Pair<Int, Int>>): Boolean {
        positions.forEach { pos ->
            val indexNegative = pos.first < 0 || pos.second < 0
            val indexTooBig = pos.first >= gridSize!! || pos.second >= gridSize!!
            if (indexNegative || indexTooBig) {
                return false
            }

            val i = pos.first
            val j = pos.second

            if (innerGrid!![i][j] != stateTurn) {
                return false
            }
        }

        return true
    }

    private fun disableAllButtons() {
        gridView!!.children.forEach { ch ->
            (ch as TableRow).children.forEach { btn ->
                (btn as Button).isEnabled = false
            }
        }
    }

    private fun checkIfDraw(): Boolean {
        return occupiedCellsCount == gridSize!! * gridSize!!
    }

    private fun changeTurn(tvTitle: TextView) {
        playerTurn = when (playerTurn) {
            Player.O -> Player.X
            Player.X -> Player.O
        }
        tvTitle.text = "Turno de: " + mapPlayerTurnToName(playerTurn)
    }

    private fun mapPlayerTurnToCellState(turn: Player): CellState = when (turn) {
        Player.O -> CellState.O
        Player.X -> CellState.X
    }

    private fun mapPlayerTurnToName(turn: Player): String? = when (turn) {
        Player.O -> player2
        Player.X -> player1
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param gridSize The grid size of the tictactoe.
         * @return A new instance of fragment Game.
         */
        @JvmStatic
        fun newInstance(gridSize: Int) =
            Game().apply {
                arguments = Bundle().apply {
                    putInt(GRID_SIZE, gridSize)
                }
            }
    }
}