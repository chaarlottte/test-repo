package ProxyManager;

import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.Socket;
import java.net.SocketImpl;

import org.lwjgl.input.Keyboard;

import me.arithmo.gui.altmanager.Colors;
import me.arithmo.gui.altmanager.RenderUtil;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.EnumChatFormatting;
import ooo.cpacket.ruby.ClientBase;

public class GuiAddAlt extends GuiScreen {
	private final GuiAltManager manager;
	private PasswordField password;
	private GuiTextField proxy;
	private String status;
	private GuiTextField username;

	public GuiAddAlt(GuiAltManager manager) {
		this.status = (EnumChatFormatting.GRAY
				+ (ClientBase.INSTANCE.proxy != Proxy.NO_PROXY
						? ((InetSocketAddress) ClientBase.INSTANCE.proxy.address()).getHostName()
								+ ((InetSocketAddress) ClientBase.INSTANCE.proxy.address()).getPort() + ""
						: "XDD"));
		this.manager = manager;
	}

	protected void actionPerformed(GuiButton button) {
		switch (button.id) {
		case 0:
			AddAltThread login = new AddAltThread(this.username.getText(), this.password.getText(),
					ProxyVersion.SOCKSv4);
			login.start();
			break;
		case 1:
			this.mc.displayGuiScreen(this.manager);
			break;
		case 2:
			String data = null;
			try {
				data = (String) Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor);
			} catch (Exception ignored) {
				break;
			}
			if (data.contains(":")) {
				String[] credentials = data.split(":");
				this.username.setText(credentials[0]);
				this.password.setText(credentials[1]);
				this.proxy.setText("SOCKS5");
			}
			break;
		}
	}

	public void drawScreen(int i, int j, float f) {
		ScaledResolution res = new ScaledResolution(this.mc, this.mc.displayWidth, this.mc.displayHeight);
		RenderUtil.rectangle(0.0D, 0.0D, res.getScaledWidth(), res.getScaledHeight(), Colors.getColor(0));
		this.username.drawTextBox();
		this.username.setMaxStringLength(2585);
		this.password.drawTextBox();
		this.proxy.drawTextBox();
		drawCenteredString(this.fontRendererObj, "Add Proxy", this.width / 2, 20, -1);
		if (this.username.getText().isEmpty()) {
			drawString(this.mc.fontRendererObj, "IP", this.width / 2 - 96, 66, -7829368);
		}
		if (this.proxy.getText().isEmpty()) {
			drawString(this.mc.fontRendererObj, "Proxy type - SOCKS4 or SOCKS5 or AUTHSOCKS5 (if password needed)",
					this.width / 2 - 96, 146, -7829368);
		}
		if (this.password.getText().isEmpty()) {
			drawString(this.mc.fontRendererObj, "Port", this.width / 2 - 96, 106, -7829368);
		}
		drawCenteredString(this.fontRendererObj, this.status, this.width / 2, 30, -1);
		super.drawScreen(i, j, f);
	}

	public void initGui() {
		Keyboard.enableRepeatEvents(true);
		this.buttonList.clear();
		this.buttonList.add(new GuiButton(0, this.width / 2 - 100, this.height / 4 + 92 + 12, "Login"));
		this.buttonList.add(new GuiButton(1, this.width / 2 - 100, this.height / 4 + 116 + 12, "Back"));
		this.buttonList.add(new GuiButton(2, this.width / 2 - 100, this.height / 4 + 116 + 36, "Import IP:Port"));
		this.username = new GuiTextField(this.eventButton, this.mc.fontRendererObj, this.width / 2 - 100, 60, 200, 20);
		this.proxy = new GuiTextField(this.eventButton, this.mc.fontRendererObj, this.width / 2 - 100, 140, 200, 20);
		this.password = new PasswordField(this.mc.fontRendererObj, this.width / 2 - 100, 100, 200, 20);
	}

	protected void keyTyped(char par1, int par2) {
		this.username.textboxKeyTyped(par1, par2);
		this.password.textboxKeyTyped(par1, par2);
		this.proxy.textboxKeyTyped(par1, par2);
		if ((par1 == '\t')
				&& ((this.username.isFocused()) || (this.proxy.isFocused()) || (this.password.isFocused()))) {
			this.username.setFocused(!this.username.isFocused());
			this.password.setFocused(!this.password.isFocused());
			this.proxy.setFocused(!this.proxy.isFocused());
		}
		if (par1 == '\r') {
			actionPerformed((GuiButton) this.buttonList.get(0));
		}
	}

	protected void mouseClicked(int par1, int par2, int par3) {
		try {
			super.mouseClicked(par1, par2, par3);
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.username.mouseClicked(par1, par2, par3);
		this.password.mouseClicked(par1, par2, par3);
		this.proxy.mouseClicked(par1, par2, par3);
	}

	private class AddAltThread extends Thread {
		private final String password;
		private final String username;
		private ProxyVersion type;

		public AddAltThread(String username, String password, ProxyVersion ver) {
			this.username = username;
			this.password = password;
			this.type = ver;
			GuiAddAlt.this.status = (EnumChatFormatting.GRAY + "Idle...");
		}

		private final void checkAndAddAlt(String username, String password, ProxyVersion ver) {
			try {
				if (!password.equals("1080")) {
					Socket socks = new Socket(new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(username, Integer.parseInt(password))));
				Class clazzSocks = socks.getClass();
				Method setSockVersion = null;
				Field sockImplField = null;
				SocketImpl socksimpl = null;
				try {
					sockImplField = clazzSocks.getDeclaredField("impl");
					sockImplField.setAccessible(true);
					socksimpl = (SocketImpl) sockImplField.get(socks);
					Class clazzSocksImpl = socksimpl.getClass();
					setSockVersion = clazzSocksImpl.getDeclaredMethod("setV4");
					setSockVersion.setAccessible(true);
					if (null != setSockVersion) {
						setSockVersion.invoke(socksimpl);
					}
					sockImplField.set(socks, socksimpl);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				socks.connect(new InetSocketAddress("httpbin.org", 80));
				if (!socks.isConnected()) {
					GuiAddAlt.this.status = "Unable to reach SOCKS4 Proxy";
					return;
				}
				socks.close();
				}
				else {
					Authenticator.setDefault(null);
					Socket socks = new Socket(new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(username, Integer.parseInt(password))));
					socks.connect(new InetSocketAddress("httpbin.org", 80));
					if (!socks.isConnected()) {
						GuiAddAlt.this.status = "Unable to reach SOCKS4 Proxy";
						return;
					}
					socks.close();
				}
			} catch (
			Exception nigger) {
				nigger.printStackTrace();
				if (true) {
					GuiAddAlt.this.status = "Unable to reach SOCKS4 Proxy";
					return;
				}
			}
			SocksProxyManager.registry.add(new SocksProxy(username, password, ver));
			try {
				ClientBase.INSTANCE.getFileMgr().getFile(Proxies.class).saveFile();
			} catch (Exception localException) {

			}
			GuiAddAlt.this.status = ("Proxy added. (" + username + ":" + password + ")");
		}

		public void run() {
			if (this.password.equals("")) {
				GuiAddAlt.this.status = (EnumChatFormatting.GREEN + "IP & Port needed");
				return;
			}
			GuiAddAlt.this.status = (EnumChatFormatting.AQUA + "Checking Proxy...");
			checkAndAddAlt(this.username, this.password, type);
		}
	}
}
