package com.example.demo.controller;

import com.example.demo.assemblers.CommentAssembler;
import com.example.demo.assemblers.CommentDTOAssembler;
import com.example.demo.dto.CommentDTO;
import com.example.demo.persistences.Comment;
import com.example.demo.persistences.Task;
import com.example.demo.services.CommentService;
import com.example.demo.services.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("fyp/api/comment")
public class CommentController {
    private final CommentService commentService;
    private final CommentAssembler commentAssembler;
    private final TaskService taskService;
    private final CommentDTOAssembler commentDTOAssembler;

    @Autowired
    public CommentController(CommentService commentService,
                             CommentAssembler commentAssembler,
                             TaskService taskService,
                             CommentDTOAssembler commentDTOAssembler) {

        super();
        this.commentService = commentService;
        this.commentAssembler = commentAssembler;
        this.taskService = taskService;
        this.commentDTOAssembler =commentDTOAssembler;
    }

    //This might no longer be necessary
    @GetMapping("/task")
    public CollectionModel<EntityModel<CommentDTO>> findCommentbyTaskId(@RequestParam int task_id) {

        Task task = this.taskService.findTaskByID(task_id);
        List<Comment> commentsbytaskid = commentService.findCommentByTask(task);
        List<CommentDTO> commentDTOList =  this.commentService.toDTOList(commentsbytaskid);
        List<EntityModel<CommentDTO>> result = commentDTOList.stream()
                                                             .map(commentDTOAssembler::toModel)
                                                             .collect(Collectors.toList());

        return CollectionModel.of(result, linkTo(methodOn(CommentController.class)
                                          .findCommentbyTaskId(task_id))
                                          .withSelfRel());
    }



    // This method use the persistence itself to send and receive data
    @PostMapping(value = "/add/{task_id}")
    public ResponseEntity<EntityModel<Comment>> addComment (@PathVariable("task_id") int task_id,
                                                            @RequestBody Comment newComment) {

        Task task = this.taskService.findTaskByID(task_id);
        newComment.setTask(task);
        EntityModel<Comment> entityModel = commentAssembler.toModel(commentService.addComment(newComment));

        return ResponseEntity.created(entityModel.getRequiredLink("byTaskID").toUri())
                .body(entityModel);
    }

    // This method use a DTO to send and receive data
    @PostMapping(value = "/add")
    public ResponseEntity<EntityModel<CommentDTO>> addCommentbyDTO ( @RequestBody CommentDTO newCommentdto) {
        Task task = this.taskService.findTaskByID((newCommentdto.getTask_id()));

        Comment newComment = new Comment( task,
                                          newCommentdto.getUser_id(),
                                          newCommentdto.getContent(),
                                          newCommentdto.getCreated_date());

        int newCommentID =  commentService.addComment(newComment).getId();

        newCommentdto.setId(newCommentID);
        EntityModel<CommentDTO> entityModel =  commentDTOAssembler.toModel(newCommentdto);
        return ResponseEntity.created(entityModel.getRequiredLink("byTaskID").toUri())
                .body(entityModel);
    }
}
