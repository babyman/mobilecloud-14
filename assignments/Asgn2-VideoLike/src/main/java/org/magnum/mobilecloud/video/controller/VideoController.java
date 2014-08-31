/*
 *
 * Copyright 2014 Jules White
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.magnum.mobilecloud.video.controller;

import org.magnum.mobilecloud.video.VideoNotFoundException;
import org.magnum.mobilecloud.video.VideoNotLikedException;
import org.magnum.mobilecloud.video.VideoPreviouslyLikedException;
import org.magnum.mobilecloud.video.repository.VideoFileManager;
import org.magnum.mobilecloud.video.repository.Video;
import org.magnum.mobilecloud.video.repository.VideoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Array;
import java.security.Principal;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

@Controller
public class VideoController {

  @Autowired
  private VideoRepository videos;

  @Autowired
  private VideoFileManager vfm;

  /**
   * GET /video
   * - Returns the list of videos that have been added to the
   * server as JSON. The list of videos should be persisted
   * using Spring Data. The list of Video objects should be able
   * to be unmarshalled by the client into a Collection<Video>.
   * - The return content-type should be application/json, which
   * will be the default if you use @ResponseBody
   */
  @RequestMapping(value = "/video", method = RequestMethod.GET)
  public
  @ResponseBody
  Iterable<Video> getVideos() {
    return videos.findAll();
  }

  /**
   * POST /video
   * - The video metadata is provided as an application/json request
   * body. The JSON should generate a valid instance of the
   * Video class when deserialized by Spring's default
   * Jackson library.
   * - Returns the JSON representation of the Video object that
   * was stored along with any updates to that object made by the server.
   * - **_The server should store the Video in a Spring Data JPA repository.
   * If done properly, the repository should handle generating ID's._**
   * - A video should not have any likes when it is initially created.
   * - You will need to add one or more annotations to the Video object
   * in order for it to be persisted with JPA.
   */
  @RequestMapping(value = "/video", method = RequestMethod.POST)
  public
  @ResponseBody
  Video addVideo(@RequestBody Video v) {
    v.setLikes(0L);
    return videos.save(v);
  }

  /**
   * GET /video/{id}
   * - Returns the video with the given id or 404 if the video is not found.
   */
  @RequestMapping(value = "/video/{id}", method = RequestMethod.GET)
  public
  @ResponseBody
  Video getVideo(@PathVariable("id") Long id) {

    Video video = videos.findOne(id);

    if (video == null) {
      throw new VideoNotFoundException();
    } else {
      return video;
    }
  }

  /**
   * POST /video/{id}/like
   * - Allows a user to like a video. Returns 200 Ok on success, 404 if the
   * video is not found, or 400 if the user has already liked the video.
   * - The service should should keep track of which users have liked a video and
   * prevent a user from liking a video twice. A POJO Video object is provided for
   * you and you will need to annotate and/or add to it in order to make it persistable.
   * - A user is only allowed to like a video once. If a user tries to like a video
   * a second time, the operation should fail and return 400 Bad Request.
   */
  @RequestMapping(value = "/video/{id}/like", method = RequestMethod.POST)
  public
  @ResponseBody
  Video likeVideo(@PathVariable("id") Long id, Principal principal) {

    Video video = videos.findOne(id);

    if (video == null) {
      throw new VideoNotFoundException();
    } else if (video.getLikedBy().contains(principal.getName())) {
      throw new VideoPreviouslyLikedException();
    } else {
      video.setLikes(video.getLikes() + 1);
      video.getLikedBy().add(principal.getName());
      videos.save(video);
      return video;
    }
  }

  /**
   * POST /video/{id}/unlike
   * - Allows a user to unlike a video that he/she previously liked. Returns 200 OK
   * on success, 404 if the video is not found, and a 400 if the user has not
   * previously liked the specified video.
   */
  @RequestMapping(value = "/video/{id}/unlike", method = RequestMethod.POST)
  public
  @ResponseBody
  Video unlikeVideo(@PathVariable("id") Long id, Principal principal) {

    Video video = videos.findOne(id);

    if (video == null) {
      throw new VideoNotFoundException();
    } else if (!video.getLikedBy().contains(principal.getName())) {
      throw new VideoNotLikedException();
    } else {
      video.setLikes(video.getLikes() - 1);
      video.getLikedBy().remove(principal.getName());
      videos.save(video);
      return video;
    }
  }

  /**
   * GET /video/{id}/likedby
   * - Returns a list of the string usernames of the users that have liked the specified
   * video. If the video is not found, a 404 error should be generated.
   */
  @RequestMapping(value = "/video/{id}/likedby", method = RequestMethod.GET)
  public
  @ResponseBody
  Iterator<String> likedBy(@PathVariable("id") Long id) {

    Video video = videos.findOne(id);

    if (video == null) {
      throw new VideoNotFoundException();
    } else {
      return video.getLikedBy().iterator();
    }
  }

  /**
   * GET /video/search/findByName?title={title}
   * - Returns a list of videos whose titles match the given parameter or an empty
   * list if none are found.
   */
  @RequestMapping(value = "/video/search/findByName", method = RequestMethod.GET)
  public
  @ResponseBody
  Iterator<Video> likeVideo(@RequestParam("title") String title) {

    return videos.findByName(title).iterator();
  }

  /**
   * GET /video/search/findByDurationLessThan?duration={duration}
   * - Returns a list of videos whose durations are less than the given parameter or
   * an empty list if none are found.
   */
  @RequestMapping(value = "/video/search/findByDurationLessThan", method = RequestMethod.GET)
  public
  @ResponseBody
  Iterator<Video> findByDurationLessThan(@RequestParam("duration") long duration) {

    return videos.findByDurationLessThan(duration).iterator();
  }

}

