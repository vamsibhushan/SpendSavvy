package com.example.spendsavvy.ui.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.spendsavvy.R
import com.example.spendsavvy.viewmodels.LimitViewModel
import com.example.spendsavvy.widget.ExpenseTextView


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LimitScreen(
    viewModel: LimitViewModel = viewModel(), navController: NavController
) {

    val context = LocalContext.current

    // State variables
    val selectedCategory = remember { mutableStateOf("Grocery") } // Default selected category
    val selectedTimePeriod = remember { mutableStateOf("Monthly") } // Default time period
    val limitAmount = remember { mutableStateOf("") } // Amount to set for limit

    Surface(modifier = Modifier.fillMaxSize()) {
        ConstraintLayout(modifier = Modifier.fillMaxSize()) {
            val (nameRow, card, topBar) = createRefs()

            // Top bar with back button and title
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
                    text = "Set Limit",
                    color = Color.White,
                    modifier = Modifier
                        .padding(16.dp)
                        .align(Alignment.Center),
                    fontSize = 25.sp,
                )
            }

            // Data form for limit settings
            Column(modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
                .shadow(16.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())

                .constrainAs(card) {
                    top.linkTo(nameRow.bottom)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }) {
                // Category Dropdown
                TitleComponent(title = "Category")
                ExpenseDropDown(listOfItems = listOf(
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
                    selectedCategory.value = it
                })

                Spacer(modifier = Modifier.height(24.dp))

                // Limit Amount Input
                TitleComponent(title = "Amount")
                OutlinedTextField(
                    value = limitAmount.value,
                    onValueChange = {
                        limitAmount.value = it.filter { char -> char.isDigit() || char == '.' }

                    },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { ExpenseTextView(text = "Enter amount") },

                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color.Black),
                    maxLines = 1,
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Time Period Dropdown
                TitleComponent(title = "Time Period")
                ExpenseDropDown(listOfItems = listOf("Monthly", "Yearly", "Weekly", "Daily"),
                    onItemSelected = {
                        selectedTimePeriod.value = it
                    })

                Spacer(modifier = Modifier.height(24.dp))

                // Save Button
                Button(
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2A706B), contentColor = Color.White
                    ),
                    onClick = {
                        val allFieldsValid = limitAmount.value.isNotEmpty()

                        if (allFieldsValid) {
                            viewModel.setLimit(
                                category = selectedCategory.value,
                                limit = limitAmount.value.toDoubleOrNull() ?: 0.0,
                                timePeriod = selectedTimePeriod.value
                            )

                            Toast.makeText(context, "Limit Saved", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(
                                context, "Please fill in all the details", Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Text("Save Limit")
                }
            }
        }
    }
}








