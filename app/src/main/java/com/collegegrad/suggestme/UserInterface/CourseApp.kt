package com.collegegrad.suggestme.userinterface

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.collegegrad.suggestme.R
import com.collegegrad.suggestme.dataclass.Course

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseApp() {
    // Sample course data
    val courses = listOf(
        Course(
            id = 1,
            title = "Introduction to Machine Learning",
            description = "Learn the fundamentals of machine learning algorithms and techniques.",
            imageResId = R.drawable.ic_launcher_foreground, // You'll need to add these images to your resources
            instructor = "Dr. Sarah Johnson",
            duration = "8 weeks",
            level = "Beginner",
            url = "https://example.com/intro-ml"
        ),
        Course(
            id = 2,
            title = "Deep Learning Specialization",
            description = "Master neural networks and deep learning frameworks for advanced AI applications.",
            imageResId = R.drawable.ic_launcher_foreground, // You'll need to add these images to your resources
            instructor = "Prof. Andrew Chen",
            duration = "12 weeks",
            level = "Intermediate",
            url = "https://example.com/deep-learning"
        ),
        Course(
            id = 3,
            title = "Natural Language Processing",
            description = "Build models that understand and generate human language using transformers.",
            imageResId = R.drawable.ic_launcher_foreground, // You'll need to add these images to your resources
            instructor = "Dr. Emily Rodriguez",
            duration = "10 weeks",
            level = "Advanced",
            url = "https://example.com/nlp"
        ),
        Course(
            id = 4,
            title = "Computer Vision with AI",
            description = "Learn to build systems that can interpret and understand visual information.",
            imageResId = R.drawable.ic_launcher_foreground, // You'll need to add these images to your resources
            instructor = "Dr. Michael Wong",
            duration = "9 weeks",
            level = "Intermediate",
            url = "https://example.com/computer-vision"
        ),
        Course(
            id = 5,
            title = "AI Ethics and Governance",
            description = "Explore the ethical considerations and governance frameworks for AI systems.",
            imageResId = R.drawable.ic_launcher_foreground, // You'll need to add these images to your resources
            instructor = "Prof. Lisa Patel",
            duration = "6 weeks",
            level = "All Levels",
            url = "https://example.com/ai-ethics"
        ),
        Course(
            id = 6,
            title = "Reinforcement Learning",
            description = "Learn how to train agents to make decisions through reinforcement.",
            imageResId = R.drawable.ic_launcher_foreground, // You'll need to add these images to your resources
            instructor = "Dr. Alex Turner",
            duration = "10 weeks",
            level = "Advanced",
            url = "https://example.com/reinforcement-learning"
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "AI Course Catalog",
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 340.dp),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(courses) { course ->
                    CourseCard(course = course)
                }
            }
        }
    }
}

@Composable
fun CourseCard(course: Course) {
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(course.url))
                context.startActivity(intent)
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column {
            Image(
                painter = painterResource(id = course.imageResId),
                contentDescription = course.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
                contentScale = ContentScale.Crop
            )

            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = course.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = course.description,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CourseInfoChip(text = course.level)
                    CourseInfoChip(text = course.duration)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Instructor: ${course.instructor}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(course.url))
                        context.startActivity(intent)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = "Enroll Now",
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun CourseInfoChip(text: String) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        modifier = Modifier.padding(end = 8.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun CourseCatalogTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(),
        content = content
    )
}