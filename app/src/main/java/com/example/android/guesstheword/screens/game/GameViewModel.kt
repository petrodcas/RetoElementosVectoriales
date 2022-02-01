package com.example.android.guesstheword.screens.game

import android.os.CountDownTimer
import android.text.format.DateUtils
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.google.android.material.snackbar.Snackbar

class GameViewModel : ViewModel() {

    companion object {
        //Time when the game is over.
        private const val DONE = 0L

        //countdown time interval
        private const val ONE_SECOND = 1000L

        //tOTAL TIME FOR THE GAME
        private const val COUNTDOWN_TIME = 15000L
    }


    //PRÁCTICA: determina el estado del cronómetro: En pausa o corriendo.
    private val _chronoState = MutableLiveData<Boolean>()
    val chronoState: LiveData<Boolean>
    get() = _chronoState

    //store the current time - countdown
    private val _currentTime = MutableLiveData<Long>()
    private val currentTime: LiveData<Long>
        get() = _currentTime

    //Timer
    private val timer: CountDownTimer

    //current time in MM:SS
    val currentTimeString = Transformations.map(currentTime) { time ->
        DateUtils.formatElapsedTime(time)
    }

    //detecta si esta en los ultimos segundos para poner el tiempo en rojo.
    private val _finalSeconds = MutableLiveData<Boolean>()
    val finalSeconds: LiveData<Boolean>
        get() = _finalSeconds

    // The current word
    private val _word = MutableLiveData<String>()
    val word : LiveData<String>
        get() = _word

    //Transformation para Hint
    val hintWord = Transformations.map(word) { word ->
        val pos=(1..word.length-1).random()
        "Current word has ${word.length} letters\nThe letter at position ${pos+1} is ${word[pos]}"
    }

    // The current score
    private val _score = MutableLiveData<Int>()
    val score : LiveData<Int>
        get() = _score

    private val _eventGameFinish = MutableLiveData<Boolean>()
    val eventGameFinish: LiveData<Boolean>
        get() = _eventGameFinish

    // The list of words - the front of the list is the next word to guess
    private lateinit var wordList: MutableList<String>

    //creación del objeto
    init {
        timer = object : CountDownTimer(COUNTDOWN_TIME, ONE_SECOND) {
            override fun onTick(millisUntilFinished: Long) {
                _currentTime.value = millisUntilFinished / ONE_SECOND
                if(_currentTime.value == 10L) onFinalSeconds()
            }

            override fun onFinish() {
                _currentTime.value = DONE
                onGameFinish()
            }
        }
        timer.start()
        //PRÁCTICA: Se inicializa el estado del cronómetro a activo
        _chronoState.value = true
        _word.value = ""
        _score.value = 0
        _eventGameFinish.value = false
        _finalSeconds.value = false
        Log.i("GameViewModel","GameViewModel creado")
        resetList()
        nextWord()
    }

    //método en la destrucción del objeto.
    override fun onCleared() {
        super.onCleared()
        Log.i("GameViewModel", "GameViewModel destruido!")
        timer.cancel()
    }

    /**
     * Resets the list of words and randomizes the order
     */
    private fun resetList() {
        wordList = mutableListOf(
            "queen",
            "hospital",
            "basketball",
            "cat",
            "change",
            "snail",
            "soup",
            "calendar",
            "sad",
            "desk",
            "guitar",
            "home",
            "railway",
            "zebra",
            "jelly",
            "car",
            "crow",
            "trade",
            "bag",
            "roll",
            "bubble"
        )
        wordList.shuffle()
    }

    /**
     * Moves to the next word in the list
     */
    //toma la siguiente palabra, si está vacía vuelve a resetear la lista.
    private fun nextWord() {
        if (!wordList.isEmpty()) {
            //Select and remove a word from the list
            _word.value = wordList.removeAt(0)
        } else {
            resetList()
        }
    }
    //decrementa los puntos
    fun onSkip() {
        _score.value = (score.value)?.minus(1)
        nextWord()
    }
    //aumenta los puntos.
    fun onCorrect() {
        _score.value = (score.value)?.plus(1)
        nextWord()
    }
    //se activa cuando el contador llega a cero.
    fun onGameFinish() {
        _eventGameFinish.value = true
    }
    //baja la bandera tras lanzar el método.
    fun onGameFinishComplete() {
        _eventGameFinish.value = false
    }
    //se activa cuando quedan menos de 10 segundos - en el temporizador.
    fun onFinalSeconds() {
        _finalSeconds.value = true
    }
    //baja la bandera tras cambiar el color del texto.
    fun onFinalSecondsComplete() {
        _finalSeconds.value = false
    }

    //PRÁCTICA: cambia el estado del cronómetro
    fun toggleChronoState() {
        _chronoState.value = !requireNotNull(chronoState.value)
    }
}