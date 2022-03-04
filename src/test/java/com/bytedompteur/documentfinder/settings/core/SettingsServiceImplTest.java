package com.bytedompteur.documentfinder.settings.core;

import com.bytedompteur.documentfinder.settings.adapter.in.Settings;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.skyscreamer.jsonassert.JSONAssert;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SettingsServiceImplTest {

  private static final String SAMPLE_APPLICATION_HOME_DIR = "SAMPLE_APPLICATION_HOME_DIR";

  private SettingsServiceImpl sut;

  @Mock
  FilesReadWriteAdapter mockedReadWriteAdapter;


  @BeforeEach
  void setUp() {
    sut = new SettingsServiceImpl(SAMPLE_APPLICATION_HOME_DIR, mockedReadWriteAdapter);
  }

  @Test
  void save_doesNotBreak_whenReadWriteAdapterThrows() throws IOException {
    // Arrange
    Mockito
      .when(mockedReadWriteAdapter.writeString(any(Path.class), any(CharSequence.class)))
      .thenThrow(new IOException("Test exception"));

    // Act
    var result = catchThrowable(() -> sut.save(Settings.builder().build()));

    // Assert
    assertThat(result).isNull();
  }

  @Test
  void save_doesNothing_whenParameterIsNull() throws IOException {
    // Act
    sut.save(null);

    //  Assert
    verify(mockedReadWriteAdapter, never()).writeString(any(), any());
  }

  @Test
  void save_convertsToJsonAndSaves_whenParameterIsNotNull() throws IOException, JSONException {
    // Arrange
    var settings = Settings
      .builder()
      .fileTypes(List.of("pdf", "txt"))
      .folders(List.of("folder_1", "folder_2"))
      .build();

    // Act
    sut.save(settings);

    //  Assert
    var pathArgumentCaptor = ArgumentCaptor.forClass(Path.class);
    var charSequenceArgumentCaptor = ArgumentCaptor.forClass(CharSequence.class);
    verify(mockedReadWriteAdapter)
      .writeString(pathArgumentCaptor.capture(), charSequenceArgumentCaptor.capture());

    assertThat(pathArgumentCaptor.getValue()).isEqualTo(Path.of("SAMPLE_APPLICATION_HOME_DIR/settings.json"));

    var expectedJson = new JSONObject()
      .put("folders", new JSONArray()
        .put("folder_1")
        .put("folder_2")
      )
      .put("fileTypes", new JSONArray()
        .put("pdf")
        .put("txt")
      )
      .toString();
    JSONAssert.assertEquals(charSequenceArgumentCaptor.getValue().toString(), expectedJson, false);
  }

  @Test
  void read_returnsEmptyOptional_whenReadWriteAdapterThrows() throws IOException {
    // Arrange
    Mockito
      .when(mockedReadWriteAdapter.readString(any()))
      .thenThrow(new IOException("Test exception"));

    // Act
    var result = sut.read();

    // Assert
    assertThat(result).isEmpty();
  }

  @Test
  void read_returnsSettingsObject_when_ReadWriteAdapterReturnedJson() throws JSONException, IOException {
    // Arrange
    var settingsJson = new JSONObject()
      .put("folders", new JSONArray()
        .put("folder_1")
      )
      .put("fileTypes", new JSONArray()
        .put("pdf")
      )
      .toString();

    Mockito
      .when(mockedReadWriteAdapter.readString(any()))
      .thenReturn(settingsJson);

    // Act
    var result = sut.read();

    // Assert
    var expectedSettings = Settings.builder().folders(List.of("folder_1")).fileTypes(List.of("pdf")).build();
    assertThat(result).contains(expectedSettings);
  }

  @Test
  void createSettingsFilePath_returnsPathPointingToSettingsJsonInApplicationHomeDirParameter() {
    // Act
    var result = sut.createSettingsFilePath();

    // Assert
    assertThat(result).isEqualTo(Path.of("SAMPLE_APPLICATION_HOME_DIR/settings.json"));
  }

  @Test
  void toJson_returnsPopulatedSettingsString() throws JSONException {
    // Arrange
    var settings = Settings
      .builder()
      .folders(List.of("folder_1", "folder_2"))
      .fileTypes(List.of("pdf", "txt"))
      .build();

    // Act
    var result = sut.toJson(settings);

    // Assert
    var expectedJson = new JSONObject()
      .put("folders", new JSONArray()
        .put("folder_1")
        .put("folder_2")
      )
      .put("fileTypes", new JSONArray()
        .put("pdf")
        .put("txt")
      )
      .toString();

    JSONAssert.assertEquals(result, expectedJson, false);
  }

  @Test
  void fromJson_returnsPopulatedSettingsObject() throws JSONException {
    // Arrange
    var json = new JSONObject()
      .put("fileTypes", new JSONArray()
        .put("pdf")
        .put("txt")
      )
      .put("folders", new JSONArray()
        .put("folder_1")
        .put("folder_2")
      )
      .toString();

    // Act
    var result = sut.fromJson(json);

    // Assert
    var expectedSettings = Settings
      .builder()
      .folders(List.of("folder_1", "folder_2"))
      .fileTypes(List.of("pdf", "txt"))
      .build();

    assertThat(result).isEqualTo(expectedSettings);
  }

}
