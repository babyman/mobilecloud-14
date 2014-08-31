package org.magnum.mobilecloud.video;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author evan
 *         Date: 2014-08-30
 */

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class VideoPreviouslyLikedException extends RuntimeException {

}
