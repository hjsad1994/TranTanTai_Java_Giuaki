package trantantai.trantantai;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import trantantai.trantantai.controllers.CategoryApiController;
import trantantai.trantantai.entities.Book;
import trantantai.trantantai.entities.Category;
import trantantai.trantantai.repositories.IBookRepository;
import trantantai.trantantai.repositories.ICategoryRepository;
import trantantai.trantantai.services.CategoryService;
import trantantai.trantantai.services.OAuthService;
import trantantai.trantantai.services.UserService;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CategoryApiController.class)
public class CategoryApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CategoryService categoryService;

    @MockBean
    private IBookRepository bookRepository;

    @MockBean
    private ICategoryRepository categoryRepository;

    @MockBean
    private UserService userService;

    @MockBean
    private OAuthService oAuthService;

    @Test
    @WithMockUser(roles = "USER")
    public void getAllCategories_returnsListOfCategories() throws Exception {
        // Arrange
        Category category1 = new Category("1", "Fiction");
        Category category2 = new Category("2", "Non-Fiction");
        List<Category> categories = Arrays.asList(category1, category2);
        when(categoryService.getAllCategories()).thenReturn(categories);

        // Act & Assert
        mockMvc.perform(get("/api/v1/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("1"))
                .andExpect(jsonPath("$[0].name").value("Fiction"))
                .andExpect(jsonPath("$[1].id").value("2"))
                .andExpect(jsonPath("$[1].name").value("Non-Fiction"));
    }

    @Test
    @WithMockUser(roles = "USER")
    public void getCategoryById_existingId_returnsCategory() throws Exception {
        // Arrange
        Category category = new Category("1", "Fiction");
        when(categoryService.getCategoryById("1")).thenReturn(Optional.of(category));

        // Act & Assert
        mockMvc.perform(get("/api/v1/categories/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.name").value("Fiction"));
    }

    @Test
    @WithMockUser(roles = "USER")
    public void getCategoryById_nonExistingId_returns404() throws Exception {
        // Arrange
        when(categoryService.getCategoryById("999")).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/api/v1/categories/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "USER")
    public void createCategory_validData_returns201() throws Exception {
        // Arrange
        String jsonContent = "{\"name\":\"Science Fiction\"}";

        // Act & Assert
        mockMvc.perform(post("/api/v1/categories")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonContent))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Science Fiction"));
    }

    @Test
    @WithMockUser(roles = "USER")
    public void updateCategory_existingId_returnsUpdated() throws Exception {
        // Arrange
        Category existingCategory = new Category("1", "Old Name");
        String jsonContent = "{\"name\":\"Updated Name\"}";
        
        when(categoryService.getCategoryById("1")).thenReturn(Optional.of(existingCategory));

        // Act & Assert
        mockMvc.perform(put("/api/v1/categories/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonContent))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.name").value("Updated Name"));
    }

    @Test
    @WithMockUser(roles = "USER")
    public void deleteCategory_noBooks_returns204() throws Exception {
        // Arrange
        Category category = new Category("1", "Fiction");
        when(categoryService.getCategoryById("1")).thenReturn(Optional.of(category));
        when(bookRepository.findByCategoryId("1")).thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(delete("/api/v1/categories/1")
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "USER")
    public void deleteCategory_hasBooks_returns409() throws Exception {
        // Arrange
        Category category = new Category("1", "Fiction");
        Book book = new Book();
        book.setId("book1");
        book.setTitle("Sample Book");
        book.setCategoryId("1");
        
        when(categoryService.getCategoryById("1")).thenReturn(Optional.of(category));
        when(bookRepository.findByCategoryId("1")).thenReturn(List.of(book));

        // Act & Assert
        mockMvc.perform(delete("/api/v1/categories/1")
                        .with(csrf()))
                .andExpect(status().isConflict());
    }
}
