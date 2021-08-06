package com.upgrad.FoodOrderingApp.service.entity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;

@Entity
@Table(name="address")
@NamedQueries({
        @NamedQuery(name = "getAllAddresses", query = "SELECT u FROM AddressEntity u"),
        @NamedQuery(name = "getAddressByUuid", query = "SELECT u FROM AddressEntity u WHERE uuid=:uuid")
})
public class AddressEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private Integer id;

    @Column(name="uuid")
    @Size(max=200)
    @NotNull
    private String uuid;

    @Column(name="flat_buil_number")
    @Size(max=255)
    @NotNull
    private String flatBuildingNumber;

    @Column(name="locality")
    @Size(max=255)
    @NotNull
    private String locality;

    @Column(name="city")
    @Size(max=30)
    @NotNull
    private String city;

    @Column(name="pincode")
    @Size(max=30)
    @NotNull
    private String pincode;

    @ManyToOne(fetch=FetchType.EAGER)
    @JoinColumn(name="state_id")
    @NotNull
    private StateEntity stateId;

    @Column(name="active")
    private Integer active;

    public AddressEntity(){
    }

    public AddressEntity(String uuid, String flatBuilNo, String locality, String city, String pincode, StateEntity stateEntity) {
        this.uuid = uuid;
        this.flatBuildingNumber = flatBuilNo;
        this.locality = locality;
        this.city = city;
        this.pincode = pincode;
        this.stateId = stateEntity;
        this.active = 1;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getFlatBuildingNumber() {
        return flatBuildingNumber;
    }

    public void setFlatBuildingNumber(String flatBuildingNumber) {
        this.flatBuildingNumber = flatBuildingNumber;
    }

    public String getLocality() {
        return locality;
    }

    public void setLocality(String locality) {
        this.locality = locality;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getPincode() {
        return pincode;
    }

    public void setPincode(String pincode) {
        this.pincode = pincode;
    }

    public StateEntity getStateId() {
        return stateId;
    }

    public void setStateId(StateEntity stateId) {
        this.stateId = stateId;
    }

    public Integer getActive() {
        return active;
    }

    public void setActive(Integer active) {
        this.active = active;
    }
}
