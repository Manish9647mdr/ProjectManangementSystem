package com.tkk.service;

import com.tkk.modal.Chat;
import com.tkk.modal.Project;
import com.tkk.modal.User;
import com.tkk.repository.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProjectServiceImpl implements ProjectService {
    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserService userService;
    
    private ChatService chatService;

    @Override
    public Project createProject(Project project, User user) throws Exception {
        Project createProject = new Project();
        createProject.setOwner(user);
        createProject.setTags(project.getTags());
        createProject.setName(project.getName());
        createProject.setCategory(project.getCategory());
        createProject.setDescription(project.getDescription());
        createProject.getTeam().add(user);

        Project savedProject = projectRepository.save(createProject);

        Chat chat = new Chat();
        chat.setProject(savedProject);

        Chat projectChat = chatService.createChat(chat);
        savedProject.setChat(projectChat);

        return savedProject;
    }

    @Override
    public List<Project> getProjectByTeam(User user, String category, String tag) throws Exception {
        List<Project> projects = projectRepository.findByTeamContainingOrOwner(user, user);

        if (category != null) {
            projects = projects.stream().filter(project -> project.getCategory().equals(category))
                    .collect(Collectors.toList());
        }
        if (tag != null) {
            projects = projects.stream().filter(project -> project.getTags().contains(tag))
                    .collect(Collectors.toList());
        }
        return projects;
    }

    @Override
    public Project getProjectById(Long projectId) throws Exception {
        Optional<Project> optionalProject = projectRepository.findById(projectId);
        if (optionalProject.isEmpty()) {
            throw new Exception("Project is not found");
        }
        return optionalProject.get();
    }

    @Override
    public void deleteProject(Long projectId, Long userId) throws Exception {
        getProjectById(projectId);
        projectRepository.deleteById(projectId);
    }

    @Override
    public Project updateProject(Project updatedProject, Long id) throws Exception {
        Project project = getProjectById(id);

        project.setName(updatedProject.getName());
        project.setDescription(updatedProject.getDescription());
        project.setTags(updatedProject.getTags());
        return projectRepository.save(project);
    }

    @Override
    public void addUserToProject(Long projectId, Long userId) throws Exception {
        Project project = getProjectById(projectId);
        User user = userService.findUserById(userId);
        if (!project.getTeam().contains(user)) {
            project.getChat().getUser().add(user);
            project.getTeam().add(user);
        }
        projectRepository.save(project);
    }

    @Override
    public void removeUserFromProject(Long projectId, Long userId) throws Exception {
        Project project = getProjectById(projectId);
        User user = userService.findUserById(userId);
        if (project.getTeam().contains(user)) {
            project.getChat().getUser().remove(user);
            project.getTeam().remove(user);
        }
        projectRepository.save(project);
    }

    @Override
    public Chat getChatByProjectId(Long projectId) throws Exception {
        Project project = getProjectById(projectId);
        return project.getChat();
    }

    @Override
    public List<Project> searchProjects(String keyword, User user) throws Exception {
        String partialName = "%" + keyword + "%";
        return projectRepository.findByNameContainingAndTeamContains(keyword, user);
    }
}
