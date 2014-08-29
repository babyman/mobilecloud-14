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
package org.magnum.dataup;

import org.magnum.dataup.model.Video;
import org.magnum.dataup.model.VideoStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Controller
public class VideoController {

  private Map<Long, Video> videos = new HashMap<>();

  @Autowired
  private VideoFileManager vfm;

  private long ids = 1;

  /**
   * GET /video
   * - Returns the list of videos that have been added to the
   * server as JSON. The list of videos does not have to be
   * persisted across restarts of the server. The list of
   * Video objects should be able to be unmarshalled by the
   * client into a Collection<Video>.
   * - The return content-type should be application/json, which
   * will be the default if you use @ResponseBody
   *
   * @return
   */
  @RequestMapping(value = VideoSvcApi.VIDEO_SVC_PATH, method = RequestMethod.GET)
  public
  @ResponseBody
  Collection<Video> getVideos() {
    return videos.values();
  }

  /**
   * POST /video
   * - The video metadata is provided as an application/json request
   * body. The JSON should generate a valid instance of the
   * Video class when deserialized by Spring's default
   * Jackson library.
   * - Returns the JSON representation of the Video object that
   * was stored along with any updates to that object made by the server.
   * - **_The server should generate a unique identifier for the Video
   * object and assign it to the Video by calling its setId(...)
   * method._**
   * - No video should have ID = 0. All IDs should be > 0.
   * - The returned Video JSON should include this server-generated
   * identifier so that the client can refer to it when uploading the
   * binary mpeg video content for the Video.
   * - The server should also generate a "data url" for the
   * Video. The "data url" is the url of the binary data for a
   * Video (e.g., the raw mpeg data). The URL should be the _full_ URL
   * for the video and not just the path (e.g., http://localhost:8080/video/1/data would
   * be a valid data url). See the Hints section for some ideas on how to
   * generate this URL.
   *
   * @param v
   * @return
   */
  @RequestMapping(value = VideoSvcApi.VIDEO_SVC_PATH, method = RequestMethod.POST)
  public
  @ResponseBody
  Video addVideo(
      @RequestBody Video v
  ) {
    v.setId(ids++);
    v.setDataUrl("http://localhost:8080/video/1/data");  // <-- fixme - generate this url
    videos.put(v.getId(), v);
    return v;
  }

  /**
   * POST /video/{id}/data
   * - The binary mpeg data for the video should be provided in a multipart
   * request as a part with the key "data". The id in the path should be
   * replaced with the unique identifier generated by the server for the
   * Video. A client MUST *create* a Video first by sending a POST to /video
   * and getting the identifier for the newly created Video object before
   * sending a POST to /video/{id}/data.
   * - The endpoint should return a VideoStatus object with state=VideoState.READY
   * if the request succeeds and the appropriate HTTP error status otherwise.
   * VideoState.PROCESSING is not used in this assignment but is present in VideoState.
   * - Rather than a PUT request, a POST is used because, by default, Spring
   * does not support a PUT with multipart data due to design decisions in the
   * Commons File Upload library: https://issues.apache.org/jira/browse/FILEUPLOAD-197
   *
   * @return
   */
  @RequestMapping(value = VideoSvcApi.VIDEO_DATA_PATH, method = RequestMethod.POST)
  public
  @ResponseBody
  VideoStatus setData(
      @PathVariable("id") Long id,
      @RequestParam("data") MultipartFile videoData
  ) throws IOException {

    Video video = videos.get(id);


    if (video == null) {
      throw new ResourceNotFoundException();
    } else {
      System.out.println("videoData = " + videoData.getName());
      System.out.println("videoData = " + videoData.getContentType());
      video.setContentType(videoData.getContentType());
      vfm.saveVideoData(video, videoData.getInputStream());
      return new VideoStatus(VideoStatus.VideoState.READY);
    }
  }

  /**
   * GET /video/{id}/data
   * - Returns the binary mpeg data (if any) for the video with the given
   * identifier. If no mpeg data has been uploaded for the specified video,
   * then the server should return a 404 status code.
   *
   * @return
   */
  @RequestMapping(value = VideoSvcApi.VIDEO_DATA_PATH, method = RequestMethod.GET)
  public void getData(
      @PathVariable("id") Long id,
      HttpServletResponse response) throws IOException {

    Video video = videos.get(id);

    if (video == null || !vfm.hasVideoData(video)) {
      throw new ResourceNotFoundException();
    } else {
      response.setStatus(200);
      response.setContentType(video.getContentType());
      vfm.copyVideoData(video, response.getOutputStream());
    }
  }

}
