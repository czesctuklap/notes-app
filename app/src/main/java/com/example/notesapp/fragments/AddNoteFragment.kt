package com.example.notesapp.fragments

import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.navigation.findNavController
import com.example.notesapp.notifications.AlarmReceiver
import com.example.notesapp.MainActivity
import com.example.notesapp.R
import com.example.notesapp.databinding.FragmentAddNoteBinding
import com.example.notesapp.model.Note
import com.example.notesapp.viewmodel.NoteViewModel
import java.util.*

class AddNoteFragment : Fragment(R.layout.fragment_add_note), MenuProvider {

    private var addNoteBinding: FragmentAddNoteBinding? = null
    private val binding get() = addNoteBinding!!

    private lateinit var notesViewModel: NoteViewModel
    private lateinit var addNoteView: View

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        addNoteBinding = FragmentAddNoteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val categories = listOf("Work", "Personal", "Study") // Categories
        val categoryAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, categories)
        binding.addNoteCategory.adapter = categoryAdapter

        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)

        notesViewModel = (activity as MainActivity).noteViewModel
        addNoteView = view
    }

    private fun saveNote(view: View) {
        val noteTitle = binding.addNoteTitle.text.toString().trim()
        val noteDesc = binding.addNoteDesc.text.toString().trim()
        val category = binding.addNoteCategory.selectedItem.toString() // kategoria
        val importanceLevel = binding.addNoteImportance.progress // poziom istotności

        if (noteTitle.isNotEmpty()) {
            val note = Note(0, noteTitle, noteDesc, category, importanceLevel)
            notesViewModel.addNote(note)

            if (importanceLevel == 5) {
                showDateTimePicker { selectedDateTime ->
                    // Parse selectedDateTime to get individual components
                    val parts = selectedDateTime.split(" ")
                    val dateParts = parts[0].split("/")
                    val timeParts = parts[1].split(":")

                    val year = dateParts[2].toInt()
                    val month = dateParts[1].toInt() - 1 // indeksowane od 0
                    val day = dateParts[0].toInt()
                    val hour = timeParts[0].toInt()
                    val minute = timeParts[1].toInt()

                    scheduleReminder(year, month, day, hour, minute)

                    Toast.makeText(addNoteView.context, "Note saved with reminder set for $selectedDateTime", Toast.LENGTH_SHORT).show()
                    view.findNavController().popBackStack(R.id.homeFragment2, false)
                }
            }
            else {
                Toast.makeText(addNoteView.context, "Note saved!", Toast.LENGTH_SHORT).show()
                view.findNavController().popBackStack(R.id.homeFragment2, false)
            }
        } else {
            Toast.makeText(addNoteView.context, "Enter the note title!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showDateTimePicker(onDateTimeSelected: (String) -> Unit) {
        // Uzyskanie bieżącej daty i godziny
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        // Wybór daty
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDayOfMonth ->
                val timePickerDialog = TimePickerDialog(
                    requireContext(),
                    { _, selectedHour, selectedMinute ->
                        // Łączenie daty i godziny w jeden ciąg
                        val selectedDateTime = "$selectedDayOfMonth/${selectedMonth + 1}/$selectedYear $selectedHour:$selectedMinute"
                        onDateTimeSelected(selectedDateTime)
                    },
                    hour,
                    minute,
                    true
                )
                timePickerDialog.show()
            },
            year,
            month,
            day
        )

        datePickerDialog.show()
    }

    private fun scheduleReminder(selectedYear: Int, selectedMonth: Int, selectedDayOfMonth: Int, selectedHour: Int, selectedMinute: Int) {
        // ustawienie alarmu
        val selectedCalendar = Calendar.getInstance()
        selectedCalendar.set(selectedYear, selectedMonth, selectedDayOfMonth, selectedHour, selectedMinute, 0)
        val selectedTimeInMillis = selectedCalendar.timeInMillis

        val intent = Intent(requireContext(), AlarmReceiver::class.java).apply {
            putExtra("title", "Reminder")
            putExtra("message", "Don't forget your important note!")
        }

        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(),
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setExactAndAllowWhileIdle( // Używamy tej metody dla kompatybilności z trybem uśpienia
            AlarmManager.RTC_WAKEUP,
            selectedTimeInMillis,
            pendingIntent
        )

        //println("Reminder set for: $selectedTimeInMillis")

        Toast.makeText(requireContext(), "Reminder set!", Toast.LENGTH_SHORT).show()
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menu.clear()
        menuInflater.inflate(R.menu.add_note_menu, menu)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return when (menuItem.itemId) {
            R.id.saveMenu -> {
                saveNote(addNoteView)
                true
            }
            else -> false
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        addNoteBinding = null
    }
}
