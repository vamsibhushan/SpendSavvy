package com.example.spendsavvy.ui.screens


import android.app.DatePickerDialog
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.spendsavvy.R
import com.example.spendsavvy.models.Transaction
import com.example.spendsavvy.utils.Utils
import com.example.spendsavvy.viewmodels.AddTransactionViewModel
import com.example.spendsavvy.viewmodels.HomeScreenViewModel
import com.example.spendsavvy.widget.ExpenseTextView
import com.google.firebase.auth.FirebaseAuth
import java.util.Calendar


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AddTransactionScreen(
    navController: NavController,
    isIncome: Boolean,
    viewModel: AddTransactionViewModel = viewModel(),
    homeScreenViewModel: HomeScreenViewModel = viewModel()

) {
    val addExpenseState by viewModel.addTransactionState.collectAsState()

    if (addExpenseState.success) {
        LaunchedEffect(Unit) {
            viewModel.onTransactionAdded = {
                // Refresh the transactions in HomeScreenViewModel
                val userId = FirebaseAuth.getInstance().currentUser?.uid
                userId?.let { homeScreenViewModel.fetchTransactions(it) }
            }

            navController.popBackStack()  // Navigate back to the home screen
        }
    }

    if (addExpenseState.error != null) {
        Toast.makeText(LocalContext.current, addExpenseState.error, Toast.LENGTH_SHORT).show()
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        ConstraintLayout(modifier = Modifier.fillMaxSize()) {
            val (nameRow, card, topBar) = createRefs()
            Image(painter = painterResource(id = R.drawable.ic_topbar),
                contentDescription = null,
                modifier = Modifier.constrainAs(topBar) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                })

            Box(modifier = Modifier
                .fillMaxWidth()
                .padding(top = 64.dp, start = 16.dp, end = 16.dp)
                .constrainAs(nameRow) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }) {
                Image(painter = painterResource(id = R.drawable.ic_back),
                    contentDescription = null,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .clickable {
                            navController.popBackStack()
                        })
                ExpenseTextView(
                    text = "Add ${if (isIncome) "Income" else "Expense"}",
                    color = Color.White,
                    modifier = Modifier
                        .padding(16.dp)
                        .align(Alignment.Center),
                    fontSize = 25.sp,
                )
            }

            DataForm(modifier = Modifier.constrainAs(card) {
                top.linkTo(nameRow.bottom)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
            }, onAddExpenseClick = { transaction ->
                viewModel.addTransaction(transaction)
            }, isIncome
            )
        }
    }

    if (addExpenseState.isSaving) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}


@Composable
fun DataForm(
    modifier: Modifier = Modifier,
    onAddExpenseClick: (model: Transaction) -> Unit,
    isIncome: Boolean
) {
    val context = LocalContext.current


    val name = remember { mutableStateOf(if (isIncome) "Salary" else "Grocery") }
    val amount = remember { mutableStateOf("") }
    val date = remember { mutableStateOf(0L) }
    val dateDialogVisibility = remember { mutableStateOf(false) }
    val type = remember { mutableStateOf(if (isIncome) "Income" else "Expense") }

    val nameError = remember { mutableStateOf(false) }
    val amountError = remember { mutableStateOf(false) }


    Column(
        modifier = modifier
            .padding(16.dp)
            .fillMaxWidth()
            .shadow(16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Name Field
        TitleComponent(title = "name")
        ExpenseDropDown(if (isIncome) listOf(
            "Salary",
            "Paypal",
            "Upwork",
            "Freelance",
            "Investments",
            "Bonus",
            "Rental Income",
            "Other Income"
        ) else listOf(
            "Grocery",
            "Netflix",
            "Rent",
            "Paypal",
            "Starbucks",
            "Shopping",
            "Transport",
            "Utilities",
            "Dining Out",
            "Entertainment",
            "Healthcare",
            "Insurance",
            "Subscriptions",
            "Education",
            "Debt Payments",
            "Gifts & Donations",
            "Travel",
            "Other Expenses"
        ), onItemSelected = {
            name.value = it
            nameError.value = it.isEmpty() // Clear error when an item is selected
        })
        Spacer(modifier = Modifier.size(24.dp))

        // Amount Field
        TitleComponent(title = "amount")
        OutlinedTextField(value = amount.value,
            onValueChange = {
                amount.value = it.filter { it.isDigit() || it == '.' }
                amountError.value = it.isEmpty()
            },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { ExpenseTextView(text = "Enter amount") },
            isError = amountError.value,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color.Black),
            maxLines = 1,
            singleLine = true
        )


        Spacer(modifier = Modifier.size(24.dp))

        // Date Field
        TitleComponent(title = "date")
        OutlinedTextField(value = if (date.value == 0L) "" else Utils.formatDateToHumanReadableForm(
            date.value
        ),
            onValueChange = {},
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    // Show the DatePicker dialog when clicked
                    dateDialogVisibility.value = true
                },
            enabled = false, // Make the text field uneditable, since it triggers the dialog
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Black,
                unfocusedBorderColor = Color.Black,
                disabledBorderColor = Color.Black, // Set border color to black when disabled
                unfocusedContainerColor = Color.White,
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                disabledTextColor = Color.Black // Ensure text is black even when disabled
            ),
            placeholder = { ExpenseTextView(text = "Select date", color = Color.Black) })



        Spacer(modifier = Modifier.size(24.dp))

        // Add Expense Button
        Button(
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF2A706B), contentColor = Color.White
            ),
            onClick = {
                val allFieldsValid =
                    name.value.isNotEmpty() && amount.value.isNotEmpty() && date.value != 0L
                if (allFieldsValid) {
                    val model = Transaction(
                        null,
                        name.value,
                        amount.value.toDoubleOrNull() ?: 0.0,
                        Utils.formatDateToHumanReadableForm(date.value),
                        type.value
                    )
                    onAddExpenseClick(model)
                    Toast.makeText(context, "Expense Added", Toast.LENGTH_SHORT).show()
                } else {
                    // Show Toast if validation fails
                    Toast.makeText(
                        context, "Please fill in all the details", Toast.LENGTH_SHORT
                    ).show()
                }

            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),

            ) {
            ExpenseTextView(
                text = "Add ${if (isIncome) "Income" else "Expense"}",
                fontSize = 14.sp,
                color = Color.White
            )
        }
    }

    // Show DatePickerDialog
    if (dateDialogVisibility.value) {
        ExpenseDatePickerDialog(onDateSelected = {
            date.value = it
            dateDialogVisibility.value = false // Hide dialog after selection
        }, onDismiss = {
            dateDialogVisibility.value = false // Hide dialog if dismissed
        }, isDialogVisible = dateDialogVisibility.value
        )
    }
}

@Composable
fun ExpenseDatePickerDialog(
    onDateSelected: (date: Long) -> Unit, onDismiss: () -> Unit, isDialogVisible: Boolean
) {
    val calendar = Calendar.getInstance()
    val context = LocalContext.current // Ensure LocalContext is inside a Composable

    // Show the DatePickerDialog when the flag is true
    if (isDialogVisible) {
        // Create and show DatePickerDialog only when the visibility flag is true
        val datePickerDialog = DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val selectedDate = Calendar.getInstance()
                selectedDate.set(year, month, dayOfMonth)
                onDateSelected(selectedDate.timeInMillis)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        // Show the dialog
        LaunchedEffect(isDialogVisible) {
            datePickerDialog.show()
        }

        // Handle dialog dismiss action
        LaunchedEffect(Unit) {
            datePickerDialog.setOnDismissListener {
                onDismiss()
            }
        }
    }
}


@Composable
fun TitleComponent(title: String) {
    ExpenseTextView(
        text = title.uppercase(),
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
        color = Color.DarkGray
    )
    Spacer(modifier = Modifier.size(10.dp))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseDropDown(listOfItems: List<String>, onItemSelected: (item: String) -> Unit) {
    val expanded = remember {
        mutableStateOf(false)
    }
    val selectedItem = remember {
        mutableStateOf(listOfItems[0])
    }
    ExposedDropdownMenuBox(expanded = expanded.value, onExpandedChange = { expanded.value = it }) {
        OutlinedTextField(
            value = selectedItem.value,
            onValueChange = {},
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            readOnly = true,
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded.value)
            },
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Black,
                unfocusedBorderColor = Color.Black,
                disabledBorderColor = Color.Black,
                disabledTextColor = Color.Black,
                disabledPlaceholderColor = Color.Black,
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black
            )
        )
        ExposedDropdownMenu(expanded = expanded.value, onDismissRequest = { }) {
            listOfItems.forEach {
                DropdownMenuItem(text = { ExpenseTextView(text = it) }, onClick = {
                    selectedItem.value = it
                    onItemSelected(selectedItem.value)
                    expanded.value = false
                })
            }
        }
    }
}



