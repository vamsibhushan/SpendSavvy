package com.example.spendsavvy.ui.screens


import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.spendsavvy.R
import com.example.spendsavvy.models.Transaction
import com.example.spendsavvy.ui.navigation.Destinations
import com.example.spendsavvy.utils.Utils
import com.example.spendsavvy.viewmodels.HomeScreenViewModel
import com.example.spendsavvy.widget.ExpenseTextView
import com.google.firebase.auth.FirebaseAuth


@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun HomeScreen(navController: NavController, viewModel: HomeScreenViewModel = viewModel()) {
    // Collect state flows to trigger recomposition on state changes
    val isLoading by viewModel.isLoading.collectAsState()
    val isNewUser by viewModel.isNewUser.collectAsState()

    // Trigger re-fetch of transactions or user status when returning to this screen
    LaunchedEffect(Unit) {
        viewModel.checkUserStatus() // Ensures the latest user status is fetched
    }

    Box(
        modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
    ) {
        when {
            isLoading -> {
                // Show a loading indicator
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            isNewUser -> {
                // Navigate to the New User screen
                NewUserScreen(navController = navController)
            }

            else -> {
                // Navigate to the Returning User screen
                ReturningUserScreen(navController = navController)
            }
        }
    }
}


// A function to get the current user name from Firebase (or SharedPreferences, etc.)
fun getCurrentUserName(): String {
    return FirebaseAuth.getInstance().currentUser?.displayName ?: "User"
}

@Composable
fun NewUserScreen(navController: NavController) {
    Surface(modifier = Modifier.fillMaxSize(), color = Color.White) {
        ConstraintLayout(modifier = Modifier.fillMaxSize()) {
            val (topBar, titleText, subtitleText, welcomeImage, getStartedButton) = createRefs()

            // Top Bar Image as background
            Image(painter = painterResource(id = R.drawable.ic_topbar), // Replace with your top bar image
                contentDescription = null,
                contentScale = ContentScale.FillWidth,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .constrainAs(topBar) {
                        top.linkTo(parent.top)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    })

            // Title Text
            Text(text = "Welcome to SpendSavvy!",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold, color = Color.White
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(top = 100.dp, start = 16.dp, end = 16.dp)
                    .constrainAs(titleText) {
                        top.linkTo(parent.top)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    })

            // Subtitle Text
            Text(text = "Your one-stop destination for managing your finances.",
                style = MaterialTheme.typography.bodyMedium.copy(color = Color.White),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .constrainAs(subtitleText) {
                        top.linkTo(titleText.bottom, margin = 8.dp)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    })

            // Welcome Image in the middle of the screen
            Image(painter = painterResource(id = R.drawable.welcome2),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(320.dp)
                    .padding(top = 65.dp, start = 20.dp, end = 20.dp)
                    .constrainAs(welcomeImage) {
                        top.linkTo(subtitleText.bottom)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    })


            // Get Started Button
            Button(onClick = { navController.navigate(Destinations.ADD_INCOME) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2A706B), contentColor = Color.White
                ),
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier
                    .padding(horizontal = 32.dp)
                    .constrainAs(getStartedButton) {
                        bottom.linkTo(parent.bottom, margin = 32.dp)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    }) {
                Text(
                    text = "Get Started",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}


@SuppressLint("DefaultLocale", "StateFlowValueCalledInComposition")
@Composable
fun ReturningUserScreen(
    navController: NavController, viewModel: HomeScreenViewModel = viewModel()
) {
    val isLoading = viewModel.isLoading.value
    val totalIncome = viewModel.totalIncome.value
    val totalExpense = viewModel.totalExpense.value
    val balance = viewModel.balance.value
    val transactions = viewModel.transactions.value
    val userName = getCurrentUserName()

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        Surface(modifier = Modifier.fillMaxSize(), color = Color.White) {
            ConstraintLayout(modifier = Modifier.fillMaxSize()) {
                val (nameRow, card, list, topBar, add) = createRefs()

// Top Bar Image
                Image(painter = painterResource(id = R.drawable.ic_topbar),
                    contentDescription = null,
                    modifier = Modifier.constrainAs(topBar) {
                        top.linkTo(parent.top)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    })

// Name and Greeting Section
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 64.dp, start = 16.dp, end = 16.dp)
                    .constrainAs(nameRow) {
                        top.linkTo(parent.top)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    }) {
                    Column(modifier = Modifier.align(Alignment.CenterStart)) {
                        ExpenseTextView(
                            text = "WELCOME",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White
                        )
                        ExpenseTextView(
                            text = userName,
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White
                        )
                    }
                    Image(painter = painterResource(id = R.drawable.ic_notification),
                        contentDescription = null,
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .size(50.dp)
                            .absolutePadding(right = 15.dp)
                            .clickable {
                                navController.navigate(Destinations.NOTIFICATIONS)
                            }

                    )
                }

// CardItem Section (Balance, Income, Expense)
                CardItem(
                    navController = navController,
                    modifier = Modifier.constrainAs(card) {
                        top.linkTo(nameRow.bottom)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    },
                    balance = String.format("%.2f", balance),
                    income = String.format("%.2f", totalIncome),
                    expense = String.format("%.2f", totalExpense)
                )

// Transaction List Section
                TransactionList(
                    navController = navController,
                    modifier = Modifier
                        .fillMaxWidth()
                        .constrainAs(list) {
                            top.linkTo(card.bottom)
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                            bottom.linkTo(parent.bottom)
                            height = Dimension.fillToConstraints
                        },

                    list = transactions,

                    )

// Floating Action Button Section
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .constrainAs(add) {
                            bottom.linkTo(parent.bottom)
                            end.linkTo(parent.end)
                        }, contentAlignment = Alignment.BottomEnd
                ) {
                    MultiFloatingActionButton(modifier = Modifier, onAddExpenseClicked = {
                        navController.navigate(Destinations.ADD_EXPENSE)
                    }, onAddIncomeClicked = {
                        navController.navigate(Destinations.ADD_INCOME)
                    })
                }
            }
        }
    }
}

@Composable
fun TransactionList(
    navController: NavController,
    modifier: Modifier,
    list: List<Transaction>,
    title: String = "Recent Transactions",

    ) {
    LazyColumn(modifier = modifier.padding(horizontal = 16.dp)) {
        item {
            Column {
                Box(modifier = modifier.fillMaxWidth()) {
                    ExpenseTextView(
                        text = title,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    if (title == "Recent Transactions") {
                        ExpenseTextView(text = "See all",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .clickable {
                                    navController.navigate(Destinations.TRANSACTION_LIST)

                                })
                    }
                }
                Spacer(modifier = Modifier.size(12.dp))
            }
        }
        items(items = list, key = { item -> item.id ?: 0 }) { item ->
            val icon = Utils.getItemIcon(item)
            val amount = if (item.type == "Income") item.amount else item.amount * -1

            TransactionItem(
                title = item.title,
                amount = amount.toString(),
                icon = icon,
                date = Utils.formatStringDateToMonthDayYear(item.date),
                color = if (item.type == "Income") Color(0xFF4CAF50) else Color(0xFFF44336),
                Modifier
            )
        }
    }
}

@Composable
fun TransactionItem(
    title: String,
    amount: String,
    icon: Int,
    date: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    // Card for elevation and rounded corners
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White), // White background for the card
        elevation = CardDefaults.cardElevation(6.dp) // Elevation for shadow effect
    ) {
        // Row to arrange content horizontally
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp), // Padding around the content
            horizontalArrangement = Arrangement.SpaceBetween, // Space items between
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon and text in a column
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icon with circular border
                Image(
                    painter = painterResource(id = icon),
                    contentDescription = null,
                    modifier = Modifier
                        .size(51.dp)
                        .clip(CircleShape) // Circular icon
                        .border(2.dp, color = color, shape = CircleShape) // Border around the icon
                )

                Spacer(modifier = Modifier.width(12.dp)) // Space between icon and text

                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Title of the transaction
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    // Date of the transaction
                    Text(
                        text = date,
                        style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray)
                    )
                }
            }

            // Amount of the transaction aligned to the end
            Text(
                text = amount,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = color, // Color based on the transaction type (e.g., expense or income)
                modifier = Modifier.align(Alignment.CenterVertically) // Align to the right end
            )
        }
    }
}


@Composable
fun MultiFloatingActionButton(
    modifier: Modifier, onAddExpenseClicked: () -> Unit, onAddIncomeClicked: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.BottomEnd) {
        Column(
            horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
// Secondary FABs
            AnimatedVisibility(visible = expanded) {
                Column(horizontalAlignment = Alignment.End, modifier = Modifier.padding(16.dp)) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                color = Color(0xFF2F7E79), shape = RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                onAddIncomeClicked.invoke()
                            }, contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_income),
                            contentDescription = "Add Income",
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                color = Color(0xFF2F7E79), shape = RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                onAddExpenseClicked.invoke()
                            }, contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_expense),
                            contentDescription = "Add Expense",
                            tint = Color.White
                        )
                    }
                }
            }
// Main FAB
            Box(
                modifier = Modifier
                    .padding(8.dp)
                    .size(60.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(color = Color(0xFF2F7E79))
                    .clickable {
                        expanded = !expanded
                    }, contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_addbutton),
                    contentDescription = "small floating action button",
                    modifier = Modifier.size(40.dp)
                )
            }
        }
    }
}

@Composable
fun CardItem(
    navController: NavController,
    modifier: Modifier,
    balance: String,
    income: String,
    expense: String
) {
    Column(
        modifier = modifier
            .padding(16.dp)
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF2F7E79))
            .padding(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Column {
                ExpenseTextView(
                    text = "Total Balance",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )
                Spacer(modifier = Modifier.size(8.dp))
                ExpenseTextView(
                    text = balance,
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color.White,
                )
            }
            Image(
                painter = painterResource(id = R.drawable.dots_menu),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .clickable(onClick = { navController.navigate(Destinations.LIMIT) })
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            CardRowItem(
                modifier = Modifier.align(Alignment.CenterStart),
                title = "Income",
                amount = income,
                imaget = R.drawable.ic_income
            )
            Spacer(modifier = Modifier.size(8.dp))
            CardRowItem(
                modifier = Modifier.align(Alignment.CenterEnd),
                title = "Expense",
                amount = expense,
                imaget = R.drawable.ic_expense
            )
        }
    }
}

@Composable
fun CardRowItem(modifier: Modifier, title: String, amount: String, imaget: Int) {
    Column(modifier = modifier) {
        Row {
            Image(
                painter = painterResource(id = imaget),
                contentDescription = null,
            )
            Spacer(modifier = Modifier.size(8.dp))
            ExpenseTextView(
                text = title, style = MaterialTheme.typography.bodyLarge, color = Color.White
            )
        }
        Spacer(modifier = Modifier.size(4.dp))
        ExpenseTextView(
            text = amount, style = MaterialTheme.typography.bodyLarge, color = Color.White
        )
    }
}


