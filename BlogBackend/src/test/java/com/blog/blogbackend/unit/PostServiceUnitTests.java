package com.blog.blogbackend.unit;

import com.blog.blogbackend.models.Comment;
import com.blog.blogbackend.models.DTOs.NewPostDTO;
import com.blog.blogbackend.models.DTOs.PostOverviewDTO;
import com.blog.blogbackend.models.Post;
import com.blog.blogbackend.models.User;
import com.blog.blogbackend.models.Vote;
import com.blog.blogbackend.repositories.PostRepository;
import com.blog.blogbackend.services.*;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.TestPropertySource;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
public class PostServiceUnitTests {
    private PostService postService;
    @Mock
    private PostRepository postRepository;
    @Mock
    private VoteService voteService;
    @Mock
    private CommentService commentService;
    private User author;
    private Post post1, post2;
    private List<Post> testPosts;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        postService = new PostServiceImpl(postRepository, voteService, commentService);
        author = new User("johndoe", "password");
        post1 = new Post("Post1", "test content", author);
        post1.setId(1L);
        post2 = new Post("Post2", "more test content", author);
        post2.setId(2L);
        testPosts = new ArrayList<>();
        testPosts.add(post1);
        testPosts.add(post2);
    }

    @Test
    public void calculateAllRatingsUpdatesRatingsOfAllPosts() {
        when(postRepository.findAllByDeletedOrderByRatingDesc(false))
                .thenReturn(testPosts);
        when(voteService.calculateRatingForPost(any(Post.class)))
                .thenAnswer(invocation -> {
                    Post p = invocation.getArgument(0);
                    p.setRating(5);
                    return p;
                });

        postService.calculateAllRatings();

        assertEquals(5, post1.getRating());
        assertEquals(5, post2.getRating());
    }

    @Test
    public void getAllPostsOrderedByRatingReturnsPostsInDescendingOrder() {
        post1.setRating(3);
        post2.setRating(6);

        when(postRepository.findAllByDeletedOrderByRatingDesc(false))
                .thenReturn(Arrays.asList(post2, post1));

        List<Post> posts = postService.getAllPostsOrderedByRating();

        assertEquals(post2, posts.get(0));
        assertEquals(post1, posts.get(1));
    }

    @Test
    public void convertPostsToOverviewsReturnsCorrectOverviewDTOs() {
        List<PostOverviewDTO> overviews = postService.convertPostsToOverviews(testPosts);
        String overview1Title = overviews.get(0).getTitle();
        String overview1Content = overviews.get(0).getContent();
        String overview2Title = overviews.get(1).getTitle();
        String overview2Content = overviews.get(1).getContent();

        assertEquals(post1.getTitle(), overview1Title);
        assertEquals(post1.getContent(), overview1Content);
        assertEquals(post2.getTitle(), overview2Title);
        assertEquals(post2.getContent(), overview2Content);
    }

    @Test
    public void createPostAddsNewPostToRepositoryAndReturnsIt() {
        NewPostDTO postData = new NewPostDTO("New post", "this is content");
        when(postRepository.save(any(Post.class)))
                .thenAnswer(invocation -> {
                    Post p = invocation.getArgument(0);
                    testPosts.add(p);
                    return p;
                });

        Post newPost = postService.create(postData, author);

        assertEquals(3, testPosts.size());
        assertNotNull(newPost);
        assertEquals(postData.getTitle(), newPost.getTitle());
        assertEquals(postData.getContent(), newPost.getContent());
        assertEquals(author, newPost.getAuthor());
    }

    @Test
    public void getPostByIdReturnsCorrectPostForExistingId() {
        Long post1id = post1.getId();
        when(postRepository.findByIdAndDeleted(post1id, false))
                .thenReturn(Optional.of(post1));

        Post foundPost = postService.getPostById(post1id);

        assertNotNull(foundPost);
        assertEquals(post1, foundPost);
    }

    @Test
    public void getPostByIdThrowsCorrectExceptionForNonExistentId() {
        long id = 123456L;
        when(postRepository.findByIdAndDeleted(id, false))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> postService.getPostById(id));
    }

    @Test
    public void verifyAuthorDoesNotThrowExceptionForCorrectAuthor() {
        assertDoesNotThrow(() -> postService.verifyAuthor(post1, author));
    }

    @Test
    public void verifyAuthorThrowsCorrectExceptionForWrongAuthor() {
        User notAuthor = new User("jane", "password");
        notAuthor.setId(12345L);

        assertThrows(AccessDeniedException.class, () -> postService.verifyAuthor(post1, notAuthor));
    }

    @Test
    public void updatePostReturnsCorrectlyChangedPostAndSavesChangesToRepository() {
        NewPostDTO updateData = new NewPostDTO("New title", "new content");
        when(postRepository.save(post1))
                .thenAnswer(invocation -> {
                    testPosts.set(0, post1);
                    return post1;
                });

        Post updated = postService.update(post1, updateData);

        assertNotNull(updated);
        assertEquals(updateData.getTitle(), updated.getTitle());
        assertEquals(updateData.getContent(), updated.getContent());
        assertEquals(updated, testPosts.get(0));
    }

    @Test
    public void softDeletePostFlagsPostAndAllCorrespondingVotesAndCommentsAsDeleted() {
        Comment comment = new Comment("Yes, I comment my own post", author, post1);
        Vote vote = new Vote(author, post1);
        vote.setVoteValue(1);
        post1.getComments().add(comment);
        post1.getVotes().add(vote);

        doAnswer(invocation -> {
            Comment c = invocation.getArgument(0);
            c.setDeleted(true);
            return null;
        }).when(commentService).softDelete(any(Comment.class));
        doAnswer(invocation -> {
            Vote v = invocation.getArgument(0);
            v.setDeleted(true);
            return null;
        }).when(voteService).softDelete(any(Vote.class));

        postService.softDelete(post1);

        assertTrue(post1.isDeleted());
        assertTrue(comment.isDeleted());
        assertTrue(vote.isDeleted());
    }
}
