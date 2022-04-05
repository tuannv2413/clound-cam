package org.thingsboard.server.dft.enduser.entity.videoHistory;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.UUID;

@Data
@NoArgsConstructor
@Entity
@Table(name = "cl_camera_record")
public class VideoHistoryEntity {

    @Id
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;

    @Column(name = "box_id")
    private UUID boxId;

    @Column(name = "camera_id")
    private UUID cameraId;

    @Column(name = "video_url")
    private String videoUrl;

    @Column(name = "static_path")
    private String staticPath;

    @Column(name = "size")
    private Long size;

    @Column(name = "start_video_time")
    private Long startVideoTime;

    @Column(name = "end_video_time")
    private Long endVideoTime;

    @Column(name = "created_time")
    private Long createdTime;

}
