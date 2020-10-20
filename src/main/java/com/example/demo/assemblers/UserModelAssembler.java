package com.example.demo.assemblers;

import com.example.demo.controller.UserController;
import com.example.demo.persistences.User;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class UserModelAssembler implements RepresentationModelAssembler<User, EntityModel<User>> {

    @Override
    public EntityModel<User> toModel(User user){
        return EntityModel.of(user, linkTo(methodOn(UserController.class)
                                    .getUserByEmail(user.getEmail()))
                                    .withSelfRel(),
                                    linkTo(methodOn(UserController.class)
                                    .getAllUsers())
                                    .withRel("users"));
    }
}
