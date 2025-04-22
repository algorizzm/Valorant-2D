public class Weapon {
	private WeaponType type;
	private int magSize, reserveAmmoMax, bulletsInMag, reserveAmmo;
	private int fireRateMs, equipTimeMs, reloadTimeMs, sprayFireRateMs;
	private long lastFiredTime = 0;
	private boolean isReloading = false;
	private long reloadStartTime = 0;
	
	private long sprayCooldownStartTime = 0;  // Time when cooldown started

	public Weapon(WeaponType type, int magSize, int reserveAmmoMax, int fireRateMs, int equipTimeMs, int reloadTimeMs, int sprayFireRateMs) {
		this.type = type;
		this.magSize = magSize;
		this.reserveAmmoMax = reserveAmmoMax;
		this.bulletsInMag = magSize;
		this.reserveAmmo = reserveAmmoMax;
		this.fireRateMs = fireRateMs;
		this.equipTimeMs = equipTimeMs;
		this.reloadTimeMs = reloadTimeMs;
		this.sprayFireRateMs = sprayFireRateMs;
	}

	public boolean canShoot() {
		return !isReloading && bulletsInMag > 0 && System.currentTimeMillis() - lastFiredTime >= fireRateMs;
	}

	public boolean canSpray() {
	    // Can spray if not reloading, have enough bullets, and cooldown time has passed
	    return !isReloading &&
	           bulletsInMag >= 3 &&
	           (System.currentTimeMillis() - sprayCooldownStartTime >= 1500); // 1 second cooldown
	}



	public void shoot() {
		if (canShoot()) {
			bulletsInMag--;
			lastFiredTime = System.currentTimeMillis();
		}
	}
	
	public void spray() {
	    if (canSpray()) {  // Check if you can spray
	        bulletsInMag -= 3; // Spray uses 3 bullets
	        lastFiredTime = System.currentTimeMillis(); // Update last fired time
	        sprayCooldownStartTime = System.currentTimeMillis();  // Start the cooldown after spraying
	    }
	}


	public boolean shouldAutoReload() {
		return bulletsInMag <= 0 && reserveAmmo > 0;
	}

	public void startReload() {
		if (!isReloading && reserveAmmo > 0 && bulletsInMag < magSize) {
			isReloading = true;
			reloadStartTime = System.currentTimeMillis();
		}
	}

	public void updateReload() {
		if (isReloading && System.currentTimeMillis() - reloadStartTime >= reloadTimeMs) {
			int needed = magSize - bulletsInMag;
			int toReload = Math.min(needed, reserveAmmo);
			bulletsInMag += toReload;
			reserveAmmo -= toReload;
			isReloading = false;
		}
	}

	public boolean isReloading() {
		return isReloading;
	}

	public WeaponType getType() {
		return type;
	}

	public int getBulletsInMag() {
		return bulletsInMag;
	}

	public int getReserveAmmo() {
		return reserveAmmo;
	}

	public long getEquipTime() {
		return equipTimeMs;
	}
	
	public long getReloadTime() {
		return reloadTimeMs;
	}
}
