/**
 * Copyright 2013- Mark C. Slee, Heron Arts LLC
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
 * ##library.name##
 * ##library.sentence##
 * ##library.url##
 *
 * @author      ##author##
 * @modified    ##date##
 * @version     ##library.prettyVersion## (##library.version##)
 */

package heronarts.p3lx.ui.component;

import heronarts.lx.LXUtils;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.UIFocus;
import heronarts.p3lx.ui.UI2dTextComponent;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.event.KeyEvent;
import processing.event.MouseEvent;

public class UIToggleSet extends UI2dTextComponent implements UIFocus, LXParameterListener {

  private String[] options = null;

  private int[] boundaries = null;

  private int value = -1;

  private boolean evenSpacing = false;

  private DiscreteParameter parameter = null;

  public UIToggleSet() {
    this(0, 0, 0, 0);
  }

  public UIToggleSet(float x, float y, float w, float h) {
    super(x, y, w, h);
  }

  @Override
  protected void onResize() {
    computeBoundaries();
  }

  public UIToggleSet setOptions(String[] options) {
    if (this.options != options) {
      this.options = options;
      this.value = 0;
      this.boundaries = new int[options.length];
      computeBoundaries();
      redraw();
    }
    return this;
  }

  public UIToggleSet setParameter(DiscreteParameter parameter) {
    if (this.parameter != parameter) {
      if (this.parameter != null) {
        this.parameter.removeListener(this);
      }
      this.parameter = parameter;
      if (this.parameter != null) {
        this.parameter.addListener(this);
        String[] options = this.parameter.getOptions();
        if (options != null) {
          setOptions(options);
        }
        setValue(this.parameter.getValuei());
      }
    }
    return this;
  }

  public void onParameterChanged(LXParameter parameter) {
    if (parameter == this.parameter) {
      setValue(this.options[this.parameter.getValuei()]);
    }
  }

  private void computeBoundaries() {
    if (this.boundaries == null) {
      return;
    }
    if (this.evenSpacing) {
      for (int i = 0; i < this.boundaries.length; ++i) {
        this.boundaries[i] = (int) ((i + 1) * this.width / this.boundaries.length);
      }
    } else {
      int totalLength = 0;
      for (String s : this.options) {
        totalLength += s.length();
      }
      int lengthSoFar = 0;
      for (int i = 0; i < this.options.length; ++i) {
        lengthSoFar += this.options[i].length();
        this.boundaries[i] = (int) (lengthSoFar * this.width / totalLength);
      }
    }
  }

  public UIToggleSet setEvenSpacing() {
    if (!this.evenSpacing) {
      this.evenSpacing = true;
      computeBoundaries();
      redraw();
    }
    return this;
  }

  public int getValueIndex() {
    return this.value;
  }

  public String getValue() {
    return this.options[this.value];
  }

  public UIToggleSet setValue(String value) {
    for (int i = 0; i < this.options.length; ++i) {
      if (this.options[i] == value) {
        return setValue(i);
      }
    }

    // That string doesn't exist
    String optStr = "{";
    for (String str : this.options) {
      optStr += str + ",";
    }
    optStr = optStr.substring(0, optStr.length() - 1) + "}";
    throw new IllegalArgumentException("Not a valid option in UIToggleSet: "
        + value + " " + optStr);
  }

  public UIToggleSet setValue(int value) {
    if (this.value != value) {
      if (value < 0 || value >= this.options.length) {
        throw new IllegalArgumentException("Invalid index to setValue(): "
            + value);
      }
      this.value = value;
      if (this.parameter != null) {
        this.parameter.setValue(value);
      }
      onToggle(getValue());
      redraw();
    }
    return this;
  }

  @Override
  public void onDraw(UI ui, PGraphics pg) {
    pg.stroke(ui.theme.getControlBorderColor());
    pg.fill(ui.theme.getControlBackgroundColor());
    pg.rect(0, 0, this.width, this.height);
    for (int b : this.boundaries) {
      pg.line(b, 1, b, this.height - 1);
    }

    pg.noStroke();
    pg.textAlign(PConstants.CENTER, PConstants.CENTER);
    pg.textFont(hasFont() ? getFont() : ui.theme.getControlFont());
    int leftBoundary = 0;

    for (int i = 0; i < this.options.length; ++i) {
      boolean isActive = (i == this.value);
      if (isActive) {
        pg.fill(ui.theme.getPrimaryColor());
        pg.rect(leftBoundary + 1, 1, this.boundaries[i] - leftBoundary - 1,
            this.height - 1);
      }
      pg.fill(isActive ? ui.WHITE : ui.theme.getControlTextColor());
      pg.text(this.options[i], (leftBoundary + this.boundaries[i]) / 2.f,
          (int) ((this.height / 2) - 2));
      leftBoundary = this.boundaries[i];
    }
  }

  protected void onToggle(String option) {
  }

  @Override
  protected void onMousePressed(MouseEvent mouseEvent, float mx, float my) {
    for (int i = 0; i < this.boundaries.length; ++i) {
      if (mx < this.boundaries[i]) {
        setValue(i);
        break;
      }
    }
  }

  @Override
  protected void onKeyPressed(KeyEvent keyEvent, char keyChar, int keyCode) {
    if ((keyCode == java.awt.event.KeyEvent.VK_LEFT)
        || (keyCode == java.awt.event.KeyEvent.VK_DOWN)) {
      setValue(LXUtils.constrain(this.value - 1, 0, this.options.length - 1));
    } else if ((keyCode == java.awt.event.KeyEvent.VK_RIGHT)
        || (keyCode == java.awt.event.KeyEvent.VK_UP)) {
      setValue(LXUtils.constrain(this.value + 1, 0, this.options.length - 1));
    }
  }

}
